/*
 * LookupService.java
 *
 * Copyright (C) 2003 MaxMind LLC. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.maxmind.geoip;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.List;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

/**
 * Provides a lookup service for information based on an IP address. The
 * location of a database file is supplied when creating a lookup service
 * instance. The edition of the database determines what information is
 * available about an IP address. See the DatabaseInfo class for further
 * details.
 * <p>
 *
 * The following code snippet demonstrates looking up the country that an IP
 * address is from:
 *
 * <pre>
 * // First, create a LookupService instance with the location of the database.
 * LookupService lookupService = new LookupService(&quot;c:\\geoip.dat&quot;);
 * // Assume we have a String ipAddress (in dot-decimal form).
 * Country country = lookupService.getCountry(ipAddress);
 * System.out.println(&quot;The country is: &quot; + country.getName());
 * System.out.println(&quot;The country code is: &quot; + country.getCode());
 * </pre>
 *
 * In general, a single LookupService instance should be created and then reused
 * repeatedly.
 * <p>
 *
 * <i>Tip:</i> Those deploying the GeoIP API as part of a web application may
 * find it difficult to pass in a File to create the lookup service, as the
 * location of the database may vary per deployment or may even be part of the
 * web-application. In this case, the database should be added to the classpath
 * of the web-app. For example, by putting it into the WEB-INF/classes directory
 * of the web application. The following code snippet demonstrates how to create
 * a LookupService using a database that can be found on the classpath:
 *
 * <pre>
 * String fileName = getClass().getResource(&quot;/GeoIP.dat&quot;).toExternalForm()
 *         .substring(6);
 * LookupService lookupService = new LookupService(fileName);
 * </pre>
 *
 * @author Matt Tucker (matt@jivesoftware.com)
 */
public class LookupService {

    /**
     * Database file.
     */
    private RandomAccessFile file;

    private final File databaseFile;

    /**
     * Information about the database.
     */
    private DatabaseInfo databaseInfo;

    private static final Charset charset = Charset.forName("ISO-8859-1");

    private final CharsetDecoder charsetDecoder = charset.newDecoder();

    /**
     * The database type. Default is the country edition.
     */
    private byte databaseType = DatabaseInfo.COUNTRY_EDITION;

    private int[] databaseSegments;

    private int recordLength;

    private int dboptions;

    private byte[] dbbuffer;

    private byte[] index_cache;

    private long mtime;

    private int last_netmask;

    private static final int US_OFFSET = 1;

    private static final int CANADA_OFFSET = 677;

    private static final int WORLD_OFFSET = 1353;

    private static final int FIPS_RANGE = 360;

    private static final int COUNTRY_BEGIN = 16776960;

    private static final int STATE_BEGIN_REV0 = 16700000;

    private static final int STATE_BEGIN_REV1 = 16000000;

    private static final int STRUCTURE_INFO_MAX_SIZE = 20;

    private static final int DATABASE_INFO_MAX_SIZE = 100;

    public static final int GEOIP_STANDARD = 0;

    public static final int GEOIP_MEMORY_CACHE = 1;

    public static final int GEOIP_CHECK_CACHE = 2;

    public static final int GEOIP_INDEX_CACHE = 4;

    public static final int GEOIP_UNKNOWN_SPEED = 0;

    public static final int GEOIP_DIALUP_SPEED = 1;

    public static final int GEOIP_CABLEDSL_SPEED = 2;

    public static final int GEOIP_CORPORATE_SPEED = 3;

    private static final int SEGMENT_RECORD_LENGTH = 3;

    private static final int STANDARD_RECORD_LENGTH = 3;

    private static final int ORG_RECORD_LENGTH = 4;

    private static final int MAX_RECORD_LENGTH = 4;

    private static final int MAX_ORG_RECORD_LENGTH = 300;

    private static final int FULL_RECORD_LENGTH = 60;

    private final Country UNKNOWN_COUNTRY = new Country("--", "N/A");

    private static final String[] countryCode = { "--", "AP", "EU", "AD", "AE", "AF", "AG", "AI", "AL", "AM", "CW", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BM", "BN", "BO", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO", "FR", "SX", "GA", "GB", "GD", "GE", "GF", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IN", "IO", "IQ", "IR", "IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "ST", "SV", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TM", "TN", "TO", "TL", "TR", "TT", "TV", "TW", "TZ", "UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF", "WS", "YE", "YT", "RS", "ZA", "ZM", "ME", "ZW", "A1", "A2", "O1", "AX", "GG", "IM", "JE", "BL", "MF", "BQ", "SS", "O1" };

    private static final String[] countryName = { "N/A", "Asia/Pacific Region", "Europe", "Andorra", "United Arab Emirates", "Afghanistan", "Antigua and Barbuda", "Anguilla", "Albania", "Armenia", "Curacao", "Angola", "Antarctica", "Argentina", "American Samoa", "Austria", "Australia", "Aruba", "Azerbaijan", "Bosnia and Herzegovina", "Barbados", "Bangladesh", "Belgium", "Burkina Faso", "Bulgaria", "Bahrain", "Burundi", "Benin", "Bermuda", "Brunei Darussalam", "Bolivia", "Brazil", "Bahamas", "Bhutan", "Bouvet Island", "Botswana", "Belarus", "Belize", "Canada", "Cocos (Keeling) Islands", "Congo, The Democratic Republic of the", "Central African Republic", "Congo", "Switzerland", "Cote D'Ivoire", "Cook Islands", "Chile", "Cameroon", "China", "Colombia", "Costa Rica", "Cuba", "Cape Verde", "Christmas Island", "Cyprus", "Czech Republic", "Germany", "Djibouti", "Denmark", "Dominica", "Dominican Republic", "Algeria", "Ecuador", "Estonia", "Egypt", "Western Sahara", "Eritrea", "Spain", "Ethiopia", "Finland", "Fiji", "Falkland Islands (Malvinas)", "Micronesia, Federated States of", "Faroe Islands", "France", "Sint Maarten (Dutch part)", "Gabon", "United Kingdom", "Grenada", "Georgia", "French Guiana", "Ghana", "Gibraltar", "Greenland", "Gambia", "Guinea", "Guadeloupe", "Equatorial Guinea", "Greece", "South Georgia and the South Sandwich Islands", "Guatemala", "Guam", "Guinea-Bissau", "Guyana", "Hong Kong", "Heard Island and McDonald Islands", "Honduras", "Croatia", "Haiti", "Hungary", "Indonesia", "Ireland", "Israel", "India", "British Indian Ocean Territory", "Iraq", "Iran, Islamic Republic of", "Iceland", "Italy", "Jamaica", "Jordan", "Japan", "Kenya", "Kyrgyzstan", "Cambodia", "Kiribati", "Comoros", "Saint Kitts and Nevis", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Cayman Islands", "Kazakhstan", "Lao People's Democratic Republic", "Lebanon", "Saint Lucia", "Liechtenstein", "Sri Lanka", "Liberia", "Lesotho", "Lithuania", "Luxembourg", "Latvia", "Libya", "Morocco", "Monaco", "Moldova, Republic of", "Madagascar", "Marshall Islands", "Macedonia", "Mali", "Myanmar", "Mongolia", "Macau", "Northern Mariana Islands", "Martinique", "Mauritania", "Montserrat", "Malta", "Mauritius", "Maldives", "Malawi", "Mexico", "Malaysia", "Mozambique", "Namibia", "New Caledonia", "Niger", "Norfolk Island", "Nigeria", "Nicaragua", "Netherlands", "Norway", "Nepal", "Nauru", "Niue", "New Zealand", "Oman", "Panama", "Peru", "French Polynesia", "Papua New Guinea", "Philippines", "Pakistan", "Poland", "Saint Pierre and Miquelon", "Pitcairn Islands", "Puerto Rico", "Palestinian Territory", "Portugal", "Palau", "Paraguay", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saudi Arabia", "Solomon Islands", "Seychelles", "Sudan", "Sweden", "Singapore", "Saint Helena", "Slovenia", "Svalbard and Jan Mayen", "Slovakia", "Sierra Leone", "San Marino", "Senegal", "Somalia", "Suriname", "Sao Tome and Principe", "El Salvador", "Syrian Arab Republic", "Swaziland", "Turks and Caicos Islands", "Chad", "French Southern Territories", "Togo", "Thailand", "Tajikistan", "Tokelau", "Turkmenistan", "Tunisia", "Tonga", "Timor-Leste", "Turkey", "Trinidad and Tobago", "Tuvalu", "Taiwan", "Tanzania, United Republic of", "Ukraine", "Uganda", "United States Minor Outlying Islands", "United States", "Uruguay", "Uzbekistan", "Holy See (Vatican City State)", "Saint Vincent and the Grenadines", "Venezuela", "Virgin Islands, British", "Virgin Islands, U.S.", "Vietnam", "Vanuatu", "Wallis and Futuna", "Samoa", "Yemen", "Mayotte", "Serbia", "South Africa", "Zambia", "Montenegro", "Zimbabwe", "Anonymous Proxy", "Satellite Provider", "Other", "Aland Islands", "Guernsey", "Isle of Man", "Jersey", "Saint Barthelemy", "Saint Martin", "Bonaire, Saint Eustatius and Saba", "South Sudan", "Other" };

    /* init the hashmap once at startup time */
    static {
        if (countryCode.length != countryName.length) {
            throw new AssertionError("countryCode.length!=countryName.length");
        }
    }

    /**
     * Create a new lookup service using the specified database file.
     *
     * @param databaseFile
     *            String representation of the database file.
     * @throws IOException
     *             if an error occured creating the lookup service from the
     *             database file.
     */
    public LookupService(String databaseFile) throws IOException {
        this(new File(databaseFile));
    }

    /**
     * Create a new lookup service using the specified database file.
     *
     * @param databaseFile
     *            the database file.
     * @throws IOException
     *             if an error occured creating the lookup service from the
     *             database file.
     */
    public LookupService(File databaseFile) throws IOException {
        this.databaseFile = databaseFile;
        file = new RandomAccessFile(databaseFile, "r");
        init();
    }

    /**
     * Create a new lookup service using the specified database file.
     *
     * @param databaseFile
     *            String representation of the database file.
     * @param options
     *            database flags to use when opening the database GEOIP_STANDARD
     *            read database from disk GEOIP_MEMORY_CACHE cache the database
     *            in RAM and read it from RAM
     * @throws IOException
     *             if an error occured creating the lookup service from the
     *             database file.
     */
    public LookupService(String databaseFile, int options) throws IOException {
        this(new File(databaseFile), options);
    }

    /**
     * Create a new lookup service using the specified database file.
     *
     * @param databaseFile
     *            the database file.
     * @param options
     *            database flags to use when opening the database GEOIP_STANDARD
     *            read database from disk GEOIP_MEMORY_CACHE cache the database
     *            in RAM and read it from RAM
     * @throws IOException
     *             if an error occured creating the lookup service from the
     *             database file.
     */
    public LookupService(File databaseFile, int options) throws IOException {
        this.databaseFile = databaseFile;
        file = new RandomAccessFile(databaseFile, "r");
        dboptions = options;
        init();
    }

    /**
     * Reads meta-data from the database file.
     *
     * @throws IOException
     *             if an error occurs reading from the database file.
     */
    private synchronized void init() throws IOException {
        if (file == null) {
            return;
        }
        if ((dboptions & GEOIP_CHECK_CACHE) != 0) {
            new Here("Unit", 304).given(dboptions, 0).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
            new Here("Unit", 304).given(dboptions, 4).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
            new Here("Unit", 304).given(dboptions, 1).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
            mtime = databaseFile.lastModified();
        }
        file.seek(file.length() - 3);
        byte[] delim = new byte[3];
        byte[] buf = new byte[SEGMENT_RECORD_LENGTH];
        for (int i = 0; i < STRUCTURE_INFO_MAX_SIZE; i++) {
            file.readFully(delim);
            if (delim[0] == -1 && delim[1] == -1 && delim[2] == -1) {
                databaseType = file.readByte();
                if (databaseType >= 106) {
                    // Backward compatibility with databases from April 2003 and
                    // earlier
                    databaseType -= 105;
                }
                // Determine the database type.
                if (databaseType == DatabaseInfo.REGION_EDITION_REV0) {
                    databaseSegments = new int[1];
                    databaseSegments[0] = STATE_BEGIN_REV0;
                    recordLength = STANDARD_RECORD_LENGTH;
                } else if (databaseType == DatabaseInfo.REGION_EDITION_REV1) {
                    databaseSegments = new int[1];
                    databaseSegments[0] = STATE_BEGIN_REV1;
                    recordLength = STANDARD_RECORD_LENGTH;
                } else if (databaseType == DatabaseInfo.CITY_EDITION_REV0 || databaseType == DatabaseInfo.CITY_EDITION_REV1 || databaseType == DatabaseInfo.ORG_EDITION || databaseType == DatabaseInfo.ORG_EDITION_V6 || databaseType == DatabaseInfo.ISP_EDITION || databaseType == DatabaseInfo.ISP_EDITION_V6 || databaseType == DatabaseInfo.DOMAIN_EDITION || databaseType == DatabaseInfo.DOMAIN_EDITION_V6 || databaseType == DatabaseInfo.ASNUM_EDITION || databaseType == DatabaseInfo.ASNUM_EDITION_V6 || databaseType == DatabaseInfo.NETSPEED_EDITION_REV1 || databaseType == DatabaseInfo.NETSPEED_EDITION_REV1_V6 || databaseType == DatabaseInfo.CITY_EDITION_REV0_V6 || databaseType == DatabaseInfo.CITY_EDITION_REV1_V6) {
                    databaseSegments = new int[1];
                    databaseSegments[0] = 0;
                    if (databaseType == DatabaseInfo.CITY_EDITION_REV0 || databaseType == DatabaseInfo.CITY_EDITION_REV1 || databaseType == DatabaseInfo.ASNUM_EDITION_V6 || databaseType == DatabaseInfo.NETSPEED_EDITION_REV1 || databaseType == DatabaseInfo.NETSPEED_EDITION_REV1_V6 || databaseType == DatabaseInfo.CITY_EDITION_REV0_V6 || databaseType == DatabaseInfo.CITY_EDITION_REV1_V6 || databaseType == DatabaseInfo.ASNUM_EDITION) {
                        recordLength = STANDARD_RECORD_LENGTH;
                    } else {
                        recordLength = ORG_RECORD_LENGTH;
                    }
                    file.readFully(buf);
                    for (int j = 0; j < SEGMENT_RECORD_LENGTH; j++) {
                        databaseSegments[0] += (unsignedByteToInt(buf[j]) << (j * 8));
                    }
                }
                break;
            } else {
                file.seek(file.getFilePointer() - 4);
            }
        }
        if ((databaseType == DatabaseInfo.COUNTRY_EDITION) || (databaseType == DatabaseInfo.COUNTRY_EDITION_V6) || (databaseType == DatabaseInfo.PROXY_EDITION) || (databaseType == DatabaseInfo.NETSPEED_EDITION)) {
            databaseSegments = new int[1];
            databaseSegments[0] = COUNTRY_BEGIN;
            recordLength = STANDARD_RECORD_LENGTH;
        }
        if ((dboptions & GEOIP_MEMORY_CACHE) == 1) {
            new Here("Unit", 374).given(dboptions, 4).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            new Here("Unit", 374).given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkTrue(group());
            new Here("Unit", 374).given(dboptions, 0).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            int l = (int) file.length();
            dbbuffer = new byte[l];
            file.seek(0);
            file.readFully(dbbuffer, 0, l);
            databaseInfo = getDatabaseInfo();
            file.close();
        }
        if ((dboptions & GEOIP_INDEX_CACHE) != 0) {
            new Here("Unit", 382).given(dboptions, 4).given(GEOIP_INDEX_CACHE, 4).checkTrue(group());
            new Here("Unit", 382).given(dboptions, 0).given(GEOIP_INDEX_CACHE, 4).checkFalse(group());
            new Here("Unit", 382).given(dboptions, 1).given(GEOIP_INDEX_CACHE, 4).checkFalse(group());
            int l = databaseSegments[0] * recordLength * 2;
            index_cache = new byte[l];
            file.seek(0);
            file.readFully(index_cache, 0, l);
        } else {
            index_cache = null;
        }
    }

    /**
     * Closes the lookup service.
     */
    public synchronized void close() {
        try {
            if (file != null) {
                file.close();
            }
            file = null;
        } catch (IOException e) {
            // Here for backward compatibility.
        }
    }

    /**
     * @return The list of all known country names
     */
    public List<String> getAllCountryNames() {
        return Arrays.asList(Arrays.copyOf(countryName, countryName.length));
    }

    /**
     * @return The list of all known country codes
     */
    public List<String> getAllCountryCodes() {
        return Arrays.asList(Arrays.copyOf(countryCode, countryCode.length));
    }

    /**
     * Returns the country the IP address is in.
     *
     * @param ipAddress
     *            String version of an IPv6 address, i.e. "::127.0.0.1"
     * @return the country the IP address is from.
     */
    public Country getCountryV6(String ipAddress) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return UNKNOWN_COUNTRY;
        }
        return getCountryV6(addr);
    }

    /**
     * Returns the country the IP address is in.
     *
     * @param ipAddress
     *            String version of an IP address, i.e. "127.0.0.1"
     * @return the country the IP address is from.
     */
    public Country getCountry(String ipAddress) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return UNKNOWN_COUNTRY;
        }
        return getCountry(addr);
    }

    /**
     * Returns the country the IP address is in.
     *
     * @param ipAddress
     *            the IP address.
     * @return the country the IP address is from.
     */
    public synchronized Country getCountry(InetAddress ipAddress) {
        return getCountry(bytesToLong(ipAddress.getAddress()));
    }

    /**
     * Returns the country the IP address is in.
     *
     * @param addr
     *            the IP address as Inet6Address.
     * @return the country the IP address is from.
     */
    public synchronized Country getCountryV6(InetAddress addr) {
        if (file == null && (dboptions & GEOIP_MEMORY_CACHE) == 0) {
            new Here("Unit", 473).given(file, "37.xml").given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            throw new IllegalStateException("Database has been closed.");
        }
        int ret = seekCountryV6(addr) - COUNTRY_BEGIN;
        if (ret == 0) {
            return UNKNOWN_COUNTRY;
        } else {
            return new Country(countryCode[ret], countryName[ret]);
        }
    }

    /**
     * Returns the country the IP address is in.
     *
     * @param ipAddress
     *            the IP address in long format.
     * @return the country the IP address is from.
     */
    public synchronized Country getCountry(long ipAddress) {
        if (file == null && (dboptions & GEOIP_MEMORY_CACHE) == 0) {
            new Here("Unit", 492).given(file, "36.xml").given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            new Here("Unit", 492).given(file, "37.xml").given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            throw new IllegalStateException("Database has been closed.");
        }
        int ret = seekCountry(ipAddress) - COUNTRY_BEGIN;
        if (ret == 0) {
            return UNKNOWN_COUNTRY;
        } else {
            return new Country(countryCode[ret], countryName[ret]);
        }
    }

    public int getID(String ipAddress) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return 0;
        }
        return getID(bytesToLong(addr.getAddress()));
    }

    public int getID(InetAddress ipAddress) {
        return getID(bytesToLong(ipAddress.getAddress()));
    }

    public synchronized int getID(long ipAddress) {
        if (file == null && (dboptions & GEOIP_MEMORY_CACHE) == 0) {
            throw new IllegalStateException("Database has been closed.");
        }
        return seekCountry(ipAddress) - databaseSegments[0];
    }

    public int last_netmask() {
        return last_netmask;
    }

    public void netmask(int nm) {
        last_netmask = nm;
    }

    /**
     * Returns information about the database.
     *
     * @return database info.
     */
    public synchronized DatabaseInfo getDatabaseInfo() {
        if (databaseInfo != null) {
            return databaseInfo;
        }
        try {
            _check_mtime();
            boolean hasStructureInfo = false;
            byte[] delim = new byte[3];
            // Advance to part of file where database info is stored.
            file.seek(file.length() - 3);
            for (int i = 0; i < STRUCTURE_INFO_MAX_SIZE; i++) {
                int read = file.read(delim);
                if (read == 3 && (delim[0] & 0xFF) == 255 && (delim[1] & 0xFF) == 255 && (delim[2] & 0xFF) == 255) {
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 2, 43, 2 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 22, 2, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 12 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 0, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 23, 2 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 111 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 30, 7, 3 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 111, -1 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 2, 0, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 23, 2, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -17, 0, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, -1 }).checkTrue(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 111, -1, -1 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 2 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 0, 0, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 7, 3, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 43, 2, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 12, -17 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 12, -17, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 0 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 30 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 2, 43 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, -1, 23 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 0, 0, -1 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 0, 22, 2 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { 0, -1, -1 }).checkFalse(group());
                    new Here("Unit", 549).given(read, 3).given(delim, new byte[] { -1, 30, 7 }).checkFalse(group());
                    hasStructureInfo = true;
                    break;
                }
                file.seek(file.getFilePointer() - 4);
            }
            if (hasStructureInfo) {
                file.seek(file.getFilePointer() - 6);
            } else {
                // No structure info, must be pre Sep 2002 database, go back to
                // end.
                file.seek(file.length() - 3);
            }
            // Find the database info string.
            for (int i = 0; i < DATABASE_INFO_MAX_SIZE; i++) {
                file.readFully(delim);
                if (delim[0] == 0 && delim[1] == 0 && delim[2] == 0) {
                    byte[] dbInfo = new byte[i];
                    file.readFully(dbInfo);
                    // Create the database info object using the string.
                    databaseInfo = new DatabaseInfo(new String(dbInfo, charset));
                    return databaseInfo;
                }
                file.seek(file.getFilePointer() - 4);
            }
        } catch (IOException e) {
            throw new InvalidDatabaseException("Error reading database info", e);
        }
        return new DatabaseInfo("");
    }

    synchronized void _check_mtime() {
        try {
            if ((dboptions & GEOIP_CHECK_CACHE) != 0) {
                new Here("Unit", 584).given(dboptions, 0).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
                new Here("Unit", 584).given(dboptions, 4).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
                new Here("Unit", 584).given(dboptions, 1).given(GEOIP_CHECK_CACHE, 2).checkFalse(group());
                long t = databaseFile.lastModified();
                if (t != mtime) {
                    /* GeoIP Database file updated */
                    /* refresh filehandle */
                    close();
                    file = new RandomAccessFile(databaseFile, "r");
                    databaseInfo = null;
                    init();
                }
            }
        } catch (IOException e) {
            throw new InvalidDatabaseException("Database not found", e);
        }
    }

    // for GeoIP City only
    public Location getLocationV6(String str) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return null;
        }
        return getLocationV6(addr);
    }

    // for GeoIP City only
    public Location getLocation(InetAddress addr) {
        return getLocation(bytesToLong(addr.getAddress()));
    }

    // for GeoIP City only
    public Location getLocation(String str) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return null;
        }
        return getLocation(addr);
    }

    public synchronized Region getRegion(String str) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return null;
        }
        return getRegion(bytesToLong(addr.getAddress()));
    }

    public synchronized Region getRegion(InetAddress addr) {
        return getRegion(bytesToLong(addr.getAddress()));
    }

    public synchronized Region getRegion(long ipnum) {
        Region record = new Region();
        int seekRegion;
        if (databaseType == DatabaseInfo.REGION_EDITION_REV0) {
            seekRegion = seekCountry(ipnum) - STATE_BEGIN_REV0;
            char[] ch = new char[2];
            if (seekRegion >= 1000) {
                record.countryCode = "US";
                record.countryName = "United States";
                ch[0] = (char) (((seekRegion - 1000) / 26) + 65);
                ch[1] = (char) (((seekRegion - 1000) % 26) + 65);
                record.region = new String(ch);
            } else {
                record.countryCode = countryCode[seekRegion];
                record.countryName = countryName[seekRegion];
                record.region = "";
            }
        } else if (databaseType == DatabaseInfo.REGION_EDITION_REV1) {
            seekRegion = seekCountry(ipnum) - STATE_BEGIN_REV1;
            char[] ch = new char[2];
            if (seekRegion < US_OFFSET) {
                record.countryCode = "";
                record.countryName = "";
                record.region = "";
            } else if (seekRegion < CANADA_OFFSET) {
                record.countryCode = "US";
                record.countryName = "United States";
                ch[0] = (char) (((seekRegion - US_OFFSET) / 26) + 65);
                ch[1] = (char) (((seekRegion - US_OFFSET) % 26) + 65);
                record.region = new String(ch);
            } else if (seekRegion < WORLD_OFFSET) {
                record.countryCode = "CA";
                record.countryName = "Canada";
                ch[0] = (char) (((seekRegion - CANADA_OFFSET) / 26) + 65);
                ch[1] = (char) (((seekRegion - CANADA_OFFSET) % 26) + 65);
                record.region = new String(ch);
            } else {
                record.countryCode = countryCode[(seekRegion - WORLD_OFFSET) / FIPS_RANGE];
                record.countryName = countryName[(seekRegion - WORLD_OFFSET) / FIPS_RANGE];
                record.region = "";
            }
        }
        return record;
    }

    public synchronized Location getLocationV6(InetAddress addr) {
        int seekCountry;
        try {
            seekCountry = seekCountryV6(addr);
            return readCityRecord(seekCountry);
        } catch (IOException e) {
            throw new InvalidDatabaseException("Error while seting up segments", e);
        }
    }

    public synchronized Location getLocation(long ipnum) {
        int seekCountry;
        try {
            seekCountry = seekCountry(ipnum);
            return readCityRecord(seekCountry);
        } catch (IOException e) {
            throw new InvalidDatabaseException("Error while seting up segments", e);
        }
    }

    private Location readCityRecord(int seekCountry) throws IOException {
        if (seekCountry == databaseSegments[0]) {
            return null;
        }
        ByteBuffer buffer = readRecordBuf(seekCountry, FULL_RECORD_LENGTH);
        Location record = new Location();
        int country = unsignedByteToInt(buffer.get());
        // get country
        record.countryCode = countryCode[country];
        record.countryName = countryName[country];
        record.region = readString(buffer);
        record.city = readString(buffer);
        record.postalCode = readString(buffer);
        record.latitude = readAngle(buffer);
        record.longitude = readAngle(buffer);
        // get DMA code
        if (databaseType == DatabaseInfo.CITY_EDITION_REV1 && "US".equals(record.countryCode)) {
            int metroareaCombo = readMetroAreaCombo(buffer);
            record.metro_code = record.dma_code = metroareaCombo / 1000;
            record.area_code = metroareaCombo % 1000;
        }
        return record;
    }

    private ByteBuffer readRecordBuf(int seek, int maxLength) throws IOException {
        int recordPointer = seek + (2 * recordLength - 1) * databaseSegments[0];
        ByteBuffer buffer;
        if ((dboptions & GEOIP_MEMORY_CACHE) == 1) {
            new Here("Unit", 747).given(dboptions, 4).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            new Here("Unit", 747).given(dboptions, 0).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            new Here("Unit", 747).given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkTrue(group());
            buffer = ByteBuffer.wrap(dbbuffer, recordPointer, Math.min(dbbuffer.length - recordPointer, maxLength));
        } else {
            byte[] recordBuf = new byte[maxLength];
            // read from disk
            file.seek(recordPointer);
            file.read(recordBuf);
            buffer = ByteBuffer.wrap(recordBuf);
        }
        return buffer;
    }

    private String readString(ByteBuffer buffer) throws CharacterCodingException {
        int start = buffer.position();
        int oldLimit = buffer.limit();
        while (buffer.hasRemaining() && buffer.get() != 0) {
        }
        int end = buffer.position() - 1;
        String str = null;
        if (end > start) {
            buffer.position(start);
            buffer.limit(end);
            str = charsetDecoder.decode(buffer).toString();
            buffer.limit(oldLimit);
        }
        buffer.position(end + 1);
        return str;
    }

    private static float readAngle(ByteBuffer buffer) {
        if (buffer.remaining() < 3) {
            throw new InvalidDatabaseException("Unexpected end of data record when reading angle");
        }
        double num = 0;
        for (int j = 0; j < 3; j++) {
            num += unsignedByteToInt(buffer.get()) << (j * 8);
        }
        return (float) num / 10000 - 180;
    }

    private static int readMetroAreaCombo(ByteBuffer buffer) {
        if (buffer.remaining() < 3) {
            throw new InvalidDatabaseException("Unexpected end of data record when reading metro area");
        }
        int metroareaCombo = 0;
        for (int j = 0; j < 3; j++) {
            metroareaCombo += unsignedByteToInt(buffer.get()) << (j * 8);
        }
        return metroareaCombo;
    }

    public String getOrg(InetAddress addr) {
        return getOrg(bytesToLong(addr.getAddress()));
    }

    public String getOrg(String str) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return null;
        }
        return getOrg(addr);
    }

    // GeoIP Organization and ISP Edition methods
    public synchronized String getOrg(long ipnum) {
        try {
            int seekOrg = seekCountry(ipnum);
            return readOrgRecord(seekOrg);
        } catch (IOException e) {
            throw new InvalidDatabaseException("Error while reading org", e);
        }
    }

    public String getOrgV6(String str) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(str);
        } catch (UnknownHostException e) {
            return null;
        }
        return getOrgV6(addr);
    }

    // GeoIP Organization and ISP Edition methods
    public synchronized String getOrgV6(InetAddress addr) {
        try {
            int seekOrg = seekCountryV6(addr);
            return readOrgRecord(seekOrg);
        } catch (IOException e) {
            throw new InvalidDatabaseException("Error while reading org", e);
        }
    }

    private String readOrgRecord(int seekOrg) throws IOException {
        if (seekOrg == databaseSegments[0]) {
            return null;
        }
        ByteBuffer buf = readRecordBuf(seekOrg, MAX_ORG_RECORD_LENGTH);
        return readString(buf);
    }

    /**
     * Finds the country index value given an IPv6 address.
     *
     * @param addr
     *            the ip address to find in long format.
     * @return the country index.
     */
    private synchronized int seekCountryV6(InetAddress addr) {
        byte[] v6vec = addr.getAddress();
        if (v6vec.length == 4) {
            // sometimes java returns an ipv4 address for IPv6 input
            // we have to work around that feature
            // It happens for ::ffff:24.24.24.24
            byte[] t = new byte[16];
            System.arraycopy(v6vec, 0, t, 12, 4);
            v6vec = t;
        }
        byte[] buf = new byte[2 * MAX_RECORD_LENGTH];
        int[] x = new int[2];
        int offset = 0;
        _check_mtime();
        for (int depth = 127; depth >= 0; depth--) {
            readNode(buf, x, offset);
            int bnum = 127 - depth;
            int idx = bnum >> 3;
            new Here("Unit", 881).given(bnum, 29).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 25).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 12).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 38).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 2).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 33).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 20).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 37).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 7).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 24).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 11).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 30).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 16).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 17).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 39).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 26).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 13).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 34).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 21).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 6).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 1).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 27).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 14).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 9).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 18).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 5).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 35).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 22).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 32).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 0).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 28).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 15).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 8).checkEq(idx, 1);
            new Here("Unit", 881).given(bnum, 3).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 19).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 4).checkEq(idx, 0);
            new Here("Unit", 881).given(bnum, 31).checkEq(idx, 3);
            new Here("Unit", 881).given(bnum, 36).checkEq(idx, 4);
            new Here("Unit", 881).given(bnum, 23).checkEq(idx, 2);
            new Here("Unit", 881).given(bnum, 10).checkEq(idx, 1);
            int bMask = 1 << (bnum & 7 ^ 7);
            new Here("Unit", 882).given(bnum, 36).checkEq(bMask, 8);
            new Here("Unit", 882).given(bnum, 17).checkEq(bMask, 64);
            new Here("Unit", 882).given(bnum, 14).checkEq(bMask, 2);
            new Here("Unit", 882).given(bnum, 2).checkEq(bMask, 32);
            new Here("Unit", 882).given(bnum, 39).checkEq(bMask, 1);
            new Here("Unit", 882).given(bnum, 38).checkEq(bMask, 2);
            new Here("Unit", 882).given(bnum, 13).checkEq(bMask, 4);
            new Here("Unit", 882).given(bnum, 16).checkEq(bMask, 128);
            new Here("Unit", 882).given(bnum, 31).checkEq(bMask, 1);
            new Here("Unit", 882).given(bnum, 30).checkEq(bMask, 2);
            new Here("Unit", 882).given(bnum, 15).checkEq(bMask, 1);
            new Here("Unit", 882).given(bnum, 11).checkEq(bMask, 16);
            new Here("Unit", 882).given(bnum, 0).checkEq(bMask, 128);
            new Here("Unit", 882).given(bnum, 24).checkEq(bMask, 128);
            new Here("Unit", 882).given(bnum, 23).checkEq(bMask, 1);
            new Here("Unit", 882).given(bnum, 22).checkEq(bMask, 2);
            new Here("Unit", 882).given(bnum, 20).checkEq(bMask, 8);
            new Here("Unit", 882).given(bnum, 4).checkEq(bMask, 8);
            new Here("Unit", 882).given(bnum, 5).checkEq(bMask, 4);
            new Here("Unit", 882).given(bnum, 9).checkEq(bMask, 64);
            new Here("Unit", 882).given(bnum, 33).checkEq(bMask, 64);
            new Here("Unit", 882).given(bnum, 28).checkEq(bMask, 8);
            new Here("Unit", 882).given(bnum, 21).checkEq(bMask, 4);
            new Here("Unit", 882).given(bnum, 7).checkEq(bMask, 1);
            new Here("Unit", 882).given(bnum, 29).checkEq(bMask, 4);
            new Here("Unit", 882).given(bnum, 34).checkEq(bMask, 32);
            new Here("Unit", 882).given(bnum, 19).checkEq(bMask, 16);
            new Here("Unit", 882).given(bnum, 32).checkEq(bMask, 128);
            new Here("Unit", 882).given(bnum, 6).checkEq(bMask, 2);
            new Here("Unit", 882).given(bnum, 26).checkEq(bMask, 32);
            new Here("Unit", 882).given(bnum, 1).checkEq(bMask, 64);
            new Here("Unit", 882).given(bnum, 27).checkEq(bMask, 16);
            new Here("Unit", 882).given(bnum, 12).checkEq(bMask, 8);
            new Here("Unit", 882).given(bnum, 8).checkEq(bMask, 128);
            new Here("Unit", 882).given(bnum, 37).checkEq(bMask, 4);
            new Here("Unit", 882).given(bnum, 35).checkEq(bMask, 16);
            new Here("Unit", 882).given(bnum, 3).checkEq(bMask, 16);
            new Here("Unit", 882).given(bnum, 25).checkEq(bMask, 64);
            new Here("Unit", 882).given(bnum, 18).checkEq(bMask, 32);
            new Here("Unit", 882).given(bnum, 10).checkEq(bMask, 32);
            if ((v6vec[idx] & bMask) > 0) {
                new Here("Unit", 883).given(bMask, 2).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 3).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 3).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 0).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 3).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 3).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 3).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 3).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 4).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 2).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 1).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 128).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 1).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 1).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 2).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 4).given(idx, 2).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 0).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 32).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 3).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkTrue(group());
                new Here("Unit", 883).given(bMask, 64).given(idx, 2).given(v6vec, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 17, -2, -40 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 8).given(idx, 0).given(v6vec, new byte[] { 32, 1, 2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                new Here("Unit", 883).given(bMask, 16).given(idx, 1).given(v6vec, new byte[] { 42, 2, -1, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }).checkFalse(group());
                if (x[1] >= databaseSegments[0]) {
                    last_netmask = 128 - depth;
                    return x[1];
                }
                offset = x[1];
            } else {
                if (x[0] >= databaseSegments[0]) {
                    last_netmask = 128 - depth;
                    return x[0];
                }
                offset = x[0];
            }
        }
        throw new InvalidDatabaseException("Error seeking country while searching for " + addr.getHostAddress());
    }

    /**
     * Finds the country index value given an IP address.
     *
     * @param ipAddress
     *            the ip address to find in long format.
     * @return the country index.
     */
    private synchronized int seekCountry(long ipAddress) {
        byte[] buf = new byte[2 * MAX_RECORD_LENGTH];
        int[] x = new int[2];
        int offset = 0;
        _check_mtime();
        for (int depth = 31; depth >= 0; depth--) {
            readNode(buf, x, offset);
            if ((ipAddress & (1 << depth)) > 0) {
                new Here("Unit", 917).given(depth, 7).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 19).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 25).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 19).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 17).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 21).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 26).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 23).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 28).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 14).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 26).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 8).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 13).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 5).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 3).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 20).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 23).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 21).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 11).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 24).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 22).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 23).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 25).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 16).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 25).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 24).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 27).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 29).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 8).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 28).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 12).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 21).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 9).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 26).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 28).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 20).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 14).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 18).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 18).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 30).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 29).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 30).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 27).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 31).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 5).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 29).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 7).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 31).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 10).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 16).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 22).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 24).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 12).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 6).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 11).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 15).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 16).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 4).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 22).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 9).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 27).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 4).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 10).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 18).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 13).given(ipAddress, 1113372144L).checkTrue(group());
                new Here("Unit", 917).given(depth, 20).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 17).given(ipAddress, 1113372144L).checkFalse(group());
                new Here("Unit", 917).given(depth, 6).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 17).given(ipAddress, 1497535488L).checkTrue(group());
                new Here("Unit", 917).given(depth, 31).given(ipAddress, 1497535488L).checkFalse(group());
                new Here("Unit", 917).given(depth, 19).given(ipAddress, 1074921176L).checkFalse(group());
                new Here("Unit", 917).given(depth, 15).given(ipAddress, 1074921176L).checkTrue(group());
                new Here("Unit", 917).given(depth, 30).given(ipAddress, 1074921176L).checkTrue(group());
                if (x[1] >= databaseSegments[0]) {
                    last_netmask = 32 - depth;
                    return x[1];
                }
                offset = x[1];
            } else {
                if (x[0] >= databaseSegments[0]) {
                    last_netmask = 32 - depth;
                    return x[0];
                }
                offset = x[0];
            }
        }
        throw new InvalidDatabaseException("Error seeking country while searching for " + ipAddress);
    }

    private void readNode(byte[] buf, int[] x, int offset) {
        if ((dboptions & GEOIP_MEMORY_CACHE) == 1) {
            new Here("Unit", 936).given(dboptions, 4).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            new Here("Unit", 936).given(dboptions, 1).given(GEOIP_MEMORY_CACHE, 1).checkTrue(group());
            new Here("Unit", 936).given(dboptions, 0).given(GEOIP_MEMORY_CACHE, 1).checkFalse(group());
            // read from memory
            System.arraycopy(dbbuffer, 2 * recordLength * offset, buf, 0, 2 * recordLength);
        } else if ((dboptions & GEOIP_INDEX_CACHE) != 0) {
            new Here("Unit", 939).given(dboptions, 4).given(GEOIP_INDEX_CACHE, 4).checkTrue(group());
            new Here("Unit", 939).given(dboptions, 0).given(GEOIP_INDEX_CACHE, 4).checkFalse(group());
            // read from index cache
            System.arraycopy(index_cache, 2 * recordLength * offset, buf, 0, 2 * recordLength);
        } else {
            // read from disk
            try {
                file.seek(2 * recordLength * offset);
                file.read(buf);
            } catch (IOException e) {
                throw new InvalidDatabaseException("Error seeking in database", e);
            }
        }
        for (int i = 0; i < 2; i++) {
            x[i] = 0;
            for (int j = 0; j < recordLength; j++) {
                int y = buf[i * recordLength + j];
                if (y < 0) {
                    y += 256;
                }
                x[i] += (y << (j * 8));
                new Here("Unit", 958).given(x[i], 0).given(y, 111).given(j, 0).checkEq(x[i], 111);
                new Here("Unit", 958).given(x[i], 0).given(y, 28).given(j, 0).checkEq(x[i], 28);
                new Here("Unit", 958).given(x[i], 4).given(y, 0).given(j, 2).checkEq(x[i], 4);
                new Here("Unit", 958).given(x[i], 107).given(y, 0).given(j, 1).checkEq(x[i], 107);
                new Here("Unit", 958).given(x[i], 0).given(y, 31).given(j, 0).checkEq(x[i], 31);
                new Here("Unit", 958).given(x[i], 0).given(y, 32).given(j, 0).checkEq(x[i], 32);
                new Here("Unit", 958).given(x[i], 0).given(y, 29).given(j, 0).checkEq(x[i], 29);
                new Here("Unit", 958).given(x[i], 0).given(y, 116).given(j, 0).checkEq(x[i], 116);
                new Here("Unit", 958).given(x[i], 116).given(y, 0).given(j, 1).checkEq(x[i], 116);
                new Here("Unit", 958).given(x[i], 0).given(y, 105).given(j, 0).checkEq(x[i], 105);
                new Here("Unit", 958).given(x[i], 0).given(y, 27).given(j, 0).checkEq(x[i], 27);
                new Here("Unit", 958).given(x[i], 112).given(y, 0).given(j, 1).checkEq(x[i], 112);
                new Here("Unit", 958).given(x[i], 107).given(y, 0).given(j, 2).checkEq(x[i], 107);
                new Here("Unit", 958).given(x[i], 31).given(y, 0).given(j, 1).checkEq(x[i], 31);
                new Here("Unit", 958).given(x[i], 0).given(y, 33).given(j, 0).checkEq(x[i], 33);
                new Here("Unit", 958).given(x[i], 27).given(y, 0).given(j, 1).checkEq(x[i], 27);
                new Here("Unit", 958).given(x[i], 103).given(y, 0).given(j, 2).checkEq(x[i], 103);
                new Here("Unit", 958).given(x[i], 32).given(y, 0).given(j, 1).checkEq(x[i], 32);
                new Here("Unit", 958).given(x[i], 116).given(y, 0).given(j, 2).checkEq(x[i], 116);
                new Here("Unit", 958).given(x[i], 103).given(y, 0).given(j, 1).checkEq(x[i], 103);
                new Here("Unit", 958).given(x[i], 26).given(y, 0).given(j, 1).checkEq(x[i], 26);
                new Here("Unit", 958).given(x[i], 112).given(y, 0).given(j, 2).checkEq(x[i], 112);
                new Here("Unit", 958).given(x[i], 104).given(y, 0).given(j, 1).checkEq(x[i], 104);
                new Here("Unit", 958).given(x[i], 0).given(y, 115).given(j, 0).checkEq(x[i], 115);
                new Here("Unit", 958).given(x[i], 2).given(y, 0).given(j, 1).checkEq(x[i], 2);
                new Here("Unit", 958).given(x[i], 0).given(y, 104).given(j, 0).checkEq(x[i], 104);
                new Here("Unit", 958).given(x[i], 0).given(y, 112).given(j, 0).checkEq(x[i], 112);
                new Here("Unit", 958).given(x[i], 115).given(y, 0).given(j, 1).checkEq(x[i], 115);
                new Here("Unit", 958).given(x[i], 119).given(y, 0).given(j, 1).checkEq(x[i], 119);
                new Here("Unit", 958).given(x[i], 0).given(y, 101).given(j, 0).checkEq(x[i], 101);
                new Here("Unit", 958).given(x[i], 1).given(y, 0).given(j, 2).checkEq(x[i], 1);
                new Here("Unit", 958).given(x[i], 27).given(y, 0).given(j, 2).checkEq(x[i], 27);
                new Here("Unit", 958).given(x[i], 31).given(y, 0).given(j, 2).checkEq(x[i], 31);
                new Here("Unit", 958).given(x[i], 0).given(y, 30).given(j, 0).checkEq(x[i], 30);
                new Here("Unit", 958).given(x[i], 0).given(y, 117).given(j, 0).checkEq(x[i], 117);
                new Here("Unit", 958).given(x[i], 111).given(y, 0).given(j, 1).checkEq(x[i], 111);
                new Here("Unit", 958).given(x[i], 29).given(y, 0).given(j, 2).checkEq(x[i], 29);
                new Here("Unit", 958).given(x[i], 108).given(y, 0).given(j, 2).checkEq(x[i], 108);
                new Here("Unit", 958).given(x[i], 0).given(y, 106).given(j, 0).checkEq(x[i], 106);
                new Here("Unit", 958).given(x[i], 104).given(y, 0).given(j, 2).checkEq(x[i], 104);
                new Here("Unit", 958).given(x[i], 0).given(y, 110).given(j, 0).checkEq(x[i], 110);
                new Here("Unit", 958).given(x[i], 3).given(y, 0).given(j, 1).checkEq(x[i], 3);
                new Here("Unit", 958).given(x[i], 108).given(y, 0).given(j, 1).checkEq(x[i], 108);
                new Here("Unit", 958).given(x[i], 0).given(y, 2).given(j, 0).checkEq(x[i], 2);
                new Here("Unit", 958).given(x[i], 2).given(y, 0).given(j, 2).checkEq(x[i], 2);
                new Here("Unit", 958).given(x[i], 115).given(y, 0).given(j, 2).checkEq(x[i], 115);
                new Here("Unit", 958).given(x[i], 111).given(y, 0).given(j, 2).checkEq(x[i], 111);
                new Here("Unit", 958).given(x[i], 25).given(y, 0).given(j, 2).checkEq(x[i], 25);
                new Here("Unit", 958).given(x[i], 0).given(y, 119).given(j, 0).checkEq(x[i], 119);
                new Here("Unit", 958).given(x[i], 105).given(y, 0).given(j, 1).checkEq(x[i], 105);
                new Here("Unit", 958).given(x[i], 0).given(y, 108).given(j, 0).checkEq(x[i], 108);
                new Here("Unit", 958).given(x[i], 101).given(y, 0).given(j, 2).checkEq(x[i], 101);
                new Here("Unit", 958).given(x[i], 4).given(y, 0).given(j, 1).checkEq(x[i], 4);
                new Here("Unit", 958).given(x[i], 118).given(y, 0).given(j, 1).checkEq(x[i], 118);
                new Here("Unit", 958).given(x[i], 33).given(y, 0).given(j, 1).checkEq(x[i], 33);
                new Here("Unit", 958).given(x[i], 33).given(y, 0).given(j, 2).checkEq(x[i], 33);
                new Here("Unit", 958).given(x[i], 3).given(y, 0).given(j, 2).checkEq(x[i], 3);
                new Here("Unit", 958).given(x[i], 0).given(y, 113).given(j, 0).checkEq(x[i], 113);
                new Here("Unit", 958).given(x[i], 30).given(y, 0).given(j, 1).checkEq(x[i], 30);
                new Here("Unit", 958).given(x[i], 43).given(y, 2).given(j, 1).checkEq(x[i], 555);
                new Here("Unit", 958).given(x[i], 114).given(y, 0).given(j, 1).checkEq(x[i], 114);
                new Here("Unit", 958).given(x[i], 110).given(y, 0).given(j, 1).checkEq(x[i], 110);
                new Here("Unit", 958).given(x[i], 109).given(y, 0).given(j, 2).checkEq(x[i], 109);
                new Here("Unit", 958).given(x[i], 29).given(y, 0).given(j, 1).checkEq(x[i], 29);
                new Here("Unit", 958).given(x[i], 1).given(y, 0).given(j, 1).checkEq(x[i], 1);
                new Here("Unit", 958).given(x[i], 0).given(y, 102).given(j, 0).checkEq(x[i], 102);
                new Here("Unit", 958).given(x[i], 28).given(y, 0).given(j, 1).checkEq(x[i], 28);
                new Here("Unit", 958).given(x[i], 105).given(y, 0).given(j, 2).checkEq(x[i], 105);
                new Here("Unit", 958).given(x[i], 101).given(y, 0).given(j, 1).checkEq(x[i], 101);
                new Here("Unit", 958).given(x[i], 118).given(y, 0).given(j, 2).checkEq(x[i], 118);
                new Here("Unit", 958).given(x[i], 0).given(y, 114).given(j, 0).checkEq(x[i], 114);
                new Here("Unit", 958).given(x[i], 109).given(y, 0).given(j, 1).checkEq(x[i], 109);
                new Here("Unit", 958).given(x[i], 0).given(y, 1).given(j, 0).checkEq(x[i], 1);
                new Here("Unit", 958).given(x[i], 0).given(y, 103).given(j, 0).checkEq(x[i], 103);
                new Here("Unit", 958).given(x[i], 25).given(y, 0).given(j, 1).checkEq(x[i], 25);
                new Here("Unit", 958).given(x[i], 110).given(y, 0).given(j, 2).checkEq(x[i], 110);
                new Here("Unit", 958).given(x[i], 114).given(y, 0).given(j, 2).checkEq(x[i], 114);
                new Here("Unit", 958).given(x[i], 0).given(y, 3).given(j, 0).checkEq(x[i], 3);
                new Here("Unit", 958).given(x[i], 106).given(y, 0).given(j, 1).checkEq(x[i], 106);
                new Here("Unit", 958).given(x[i], 0).given(y, 25).given(j, 0).checkEq(x[i], 25);
                new Here("Unit", 958).given(x[i], 555).given(y, 0).given(j, 2).checkEq(x[i], 555);
                new Here("Unit", 958).given(x[i], 117).given(y, 0).given(j, 1).checkEq(x[i], 117);
                new Here("Unit", 958).given(x[i], 102).given(y, 0).given(j, 2).checkEq(x[i], 102);
                new Here("Unit", 958).given(x[i], 0).given(y, 43).given(j, 0).checkEq(x[i], 43);
                new Here("Unit", 958).given(x[i], 0).given(y, 109).given(j, 0).checkEq(x[i], 109);
                new Here("Unit", 958).given(x[i], 0).given(y, 4).given(j, 0).checkEq(x[i], 4);
                new Here("Unit", 958).given(x[i], 113).given(y, 0).given(j, 1).checkEq(x[i], 113);
                new Here("Unit", 958).given(x[i], 30).given(y, 0).given(j, 2).checkEq(x[i], 30);
                new Here("Unit", 958).given(x[i], 32).given(y, 0).given(j, 2).checkEq(x[i], 32);
                new Here("Unit", 958).given(x[i], 106).given(y, 0).given(j, 2).checkEq(x[i], 106);
                new Here("Unit", 958).given(x[i], 28).given(y, 0).given(j, 2).checkEq(x[i], 28);
                new Here("Unit", 958).given(x[i], 0).given(y, 26).given(j, 0).checkEq(x[i], 26);
                new Here("Unit", 958).given(x[i], 0).given(y, 118).given(j, 0).checkEq(x[i], 118);
                new Here("Unit", 958).given(x[i], 102).given(y, 0).given(j, 1).checkEq(x[i], 102);
                new Here("Unit", 958).given(x[i], 26).given(y, 0).given(j, 2).checkEq(x[i], 26);
                new Here("Unit", 958).given(x[i], 117).given(y, 0).given(j, 2).checkEq(x[i], 117);
                new Here("Unit", 958).given(x[i], 0).given(y, 107).given(j, 0).checkEq(x[i], 107);
                new Here("Unit", 958).given(x[i], 113).given(y, 0).given(j, 2).checkEq(x[i], 113);
            }
        }
    }

    /**
     * Returns the long version of an IP address given an InetAddress object.
     *
     * @param address
     *            the InetAddress.
     * @return the long form of the IP address.
     */
    private static long bytesToLong(byte[] address) {
        long ipnum = 0;
        for (int i = 0; i < 4; ++i) {
            long y = address[i];
            if (y < 0) {
                y += 256;
            }
            ipnum += y << ((3 - i) * 8);
            new Here("Unit", 977).given(ipnum, 1074855936L).given(y, 254L).given(i, 2).checkEq(ipnum, 1074920960L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 70L).given(i, 0).checkEq(ipnum, 1174405120L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 67L).given(i, 0).checkEq(ipnum, 1124073472L);
            new Here("Unit", 977).given(ipnum, 1308622848L).given(y, 26L).given(i, 1).checkEq(ipnum, 1310326784L);
            new Here("Unit", 977).given(ipnum, 1126931456L).given(y, 0L).given(i, 3).checkEq(ipnum, 1126931456L);
            new Here("Unit", 977).given(ipnum, 1124073472L).given(y, 43L).given(i, 1).checkEq(ipnum, 1126891520L);
            new Here("Unit", 977).given(ipnum, 1431830528L).given(y, 2L).given(i, 2).checkEq(ipnum, 1431831040L);
            new Here("Unit", 977).given(ipnum, 1497535488L).given(y, 0L).given(i, 3).checkEq(ipnum, 1497535488L);
            new Here("Unit", 977).given(ipnum, 1177419776L).given(y, 123L).given(i, 2).checkEq(ipnum, 1177451264L);
            new Here("Unit", 977).given(ipnum, 3739615232L).given(y, 137L).given(i, 2).checkEq(ipnum, 3739650304L);
            new Here("Unit", 977).given(ipnum, 1406018560L).given(y, 224L).given(i, 3).checkEq(ipnum, 1406018784L);
            new Here("Unit", 977).given(ipnum, 1073741824L).given(y, 17L).given(i, 1).checkEq(ipnum, 1074855936L);
            new Here("Unit", 977).given(ipnum, 1493172224L).given(y, 66L).given(i, 1).checkEq(ipnum, 1497497600L);
            new Here("Unit", 977).given(ipnum, 536936448L).given(y, 2L).given(i, 2).checkEq(ipnum, 536936960L);
            new Here("Unit", 977).given(ipnum, 1310326784L).given(y, 70L).given(i, 2).checkEq(ipnum, 1310344704L);
            new Here("Unit", 977).given(ipnum, 1406009344L).given(y, 36L).given(i, 2).checkEq(ipnum, 1406018560L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 89L).given(i, 0).checkEq(ipnum, 1493172224L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 64L).given(i, 0).checkEq(ipnum, 1073741824L);
            new Here("Unit", 977).given(ipnum, 3739650304L).given(y, 0L).given(i, 3).checkEq(ipnum, 3739650304L);
            new Here("Unit", 977).given(ipnum, 536936960L).given(y, 0L).given(i, 3).checkEq(ipnum, 536936960L);
            new Here("Unit", 977).given(ipnum, 1113325568L).given(y, 181L).given(i, 2).checkEq(ipnum, 1113371904L);
            new Here("Unit", 977).given(ipnum, 1113371904L).given(y, 240L).given(i, 3).checkEq(ipnum, 1113372144L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 85L).given(i, 0).checkEq(ipnum, 1426063360L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 222L).given(i, 0).checkEq(ipnum, 3724541952L);
            new Here("Unit", 977).given(ipnum, 1392508928L).given(y, 206L).given(i, 1).checkEq(ipnum, 1406009344L);
            new Here("Unit", 977).given(ipnum, 1074920960L).given(y, 216L).given(i, 3).checkEq(ipnum, 1074921176L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 66L).given(i, 0).checkEq(ipnum, 1107296256L);
            new Here("Unit", 977).given(ipnum, 1497497600L).given(y, 148L).given(i, 2).checkEq(ipnum, 1497535488L);
            new Here("Unit", 977).given(ipnum, 1126891520L).given(y, 156L).given(i, 2).checkEq(ipnum, 1126931456L);
            new Here("Unit", 977).given(ipnum, 1310344704L).given(y, 208L).given(i, 3).checkEq(ipnum, 1310344912L);
            new Here("Unit", 977).given(ipnum, 1177451264L).given(y, 145L).given(i, 3).checkEq(ipnum, 1177451409L);
            new Here("Unit", 977).given(ipnum, 1174405120L).given(y, 46L).given(i, 1).checkEq(ipnum, 1177419776L);
            new Here("Unit", 977).given(ipnum, 3724541952L).given(y, 230L).given(i, 1).checkEq(ipnum, 3739615232L);
            new Here("Unit", 977).given(ipnum, 536870912L).given(y, 1L).given(i, 1).checkEq(ipnum, 536936448L);
            new Here("Unit", 977).given(ipnum, 1426063360L).given(y, 88L).given(i, 1).checkEq(ipnum, 1431830528L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 78L).given(i, 0).checkEq(ipnum, 1308622848L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 32L).given(i, 0).checkEq(ipnum, 536870912L);
            new Here("Unit", 977).given(ipnum, 1431831040L).given(y, 224L).given(i, 3).checkEq(ipnum, 1431831264L);
            new Here("Unit", 977).given(ipnum, 1107296256L).given(y, 92L).given(i, 1).checkEq(ipnum, 1113325568L);
            new Here("Unit", 977).given(ipnum, 0L).given(y, 83L).given(i, 0).checkEq(ipnum, 1392508928L);
        }
        return ipnum;
    }

    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
