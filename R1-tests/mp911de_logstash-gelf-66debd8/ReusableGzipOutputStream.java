package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

/**
 * This class implements a stream filter for writing compressed data in the GZIP file format. It's an adoption of
 * {@link java.util.zip.GZIPOutputStream} but with a notable difference regarding re-usability:
 * <ul>
 * <li>Expose {@link #reset()} to reset CRC32 and the deflater</li>
 * <li>Don't write the GZIP header upon construction but expose {@link #writeHeader()}</li>
 * </ul>
 *
 * @author Mark Paluch
 * @since 1.11
 */
class ReusableGzipOutputStream extends DeflaterOutputStream {

    /**
     * CRC-32 of uncompressed data.
     */
    private final CRC32 crc = new CRC32();

    /*
     * GZIP header magic number.
     */
    private static final int GZIP_MAGIC = 0x8b1f;

    /*
     * Trailer size in bytes.
     *
     */
    private static final int TRAILER_SIZE = 8;

    /**
     * Creates a new output stream with the specified buffer size and flush mode.
     *
     * @param out the output stream
     * @exception IOException If an I/O error has occurred.
     * @exception IllegalArgumentException if {@code size <= 0}
     *
     * @since 1.7
     */
    public ReusableGzipOutputStream(OutputStream out) throws IOException {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        crc.reset();
    }

    /**
     * Writes array of bytes to the compressed output stream. This method will block until all the bytes are written.
     *
     * @param buf the data to be written
     * @param off the start offset of the data
     * @param len the length of the data
     * @exception IOException If an I/O error has occurred.
     */
    @Override
    public synchronized void write(byte[] buf, int off, int len) throws IOException {
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }

    /**
     * Finishes writing compressed data to the output stream without closing the underlying stream. Use this method when
     * applying multiple filters in succession to the same output stream.
     *
     * @exception IOException if an I/O error has occurred
     */
    @Override
    public void finish() throws IOException {
        if (!def.finished()) {
            def.finish();
            while (!def.finished()) {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                    // last deflater buffer. Fit trailer at the end
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(buf, 0, len);
                    return;
                }
                if (len > 0) {
                    out.write(buf, 0, len);
                }
            }
            // if we can't fit the trailer at the end of the last
            // deflater buffer, we write it separately
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer);
        }
    }

    /*
     * Writes GZIP member header.
     */
    public void writeHeader() throws IOException {
        out.write(new byte[] { // Magic number (short)
        (byte) GZIP_MAGIC, // Magic number (short)
        (byte) (GZIP_MAGIC >> 8), // Compression method (CM)
        Deflater.DEFLATED, // Flags (FLG)
        0, // Modification time MTIME (int)
        0, // Modification time MTIME (int)
        0, // Modification time MTIME (int)
        0, // Modification time MTIME (int)
        0, // Extra flags (XFLG)
        0, // Operating system (OS)
        0 });
    }

    /*
     * Writes GZIP member trailer to a byte array, starting at a given offset.
     */
    private void writeTrailer(byte[] buf, int offset) {
        // CRC-32 of uncompr. data
        writeInt((int) crc.getValue(), buf, offset);
        // Number of uncompr. bytes
        writeInt(def.getTotalIn(), buf, offset + 4);
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) {
        buf[offset] = (byte) (s & 0xff);
        new Here("Unit", 133).given(s, 23148).checkEq(buf[offset], 108);
        new Here("Unit", 133).given(s, 0).checkEq(buf[offset], 0);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);
        new Here("Unit", 134).given(s, 23148).checkEq(buf[offset + 1], 90);
    }

    public void reset() {
        crc.reset();
        def.reset();
    }
}
