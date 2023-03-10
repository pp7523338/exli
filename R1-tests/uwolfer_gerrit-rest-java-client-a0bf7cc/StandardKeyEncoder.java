// Copyright 2008 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.gwtorm.server;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

public class StandardKeyEncoder {

    private static final char[] hexc = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final char[] safe;

    private static final byte[] hexb;

    static {
        safe = new char[256];
        safe['-'] = '-';
        safe['_'] = '_';
        safe['.'] = '.';
        safe['!'] = '!';
        safe['~'] = '~';
        safe['*'] = '*';
        safe['\''] = '\'';
        safe['('] = '(';
        safe[')'] = ')';
        safe['/'] = '/';
        safe[' '] = '+';
        for (char c = '0'; c <= '9'; c++) safe[c] = c;
        for (char c = 'A'; c <= 'Z'; c++) safe[c] = c;
        for (char c = 'a'; c <= 'z'; c++) safe[c] = c;
        hexb = new byte['f' + 1];
        Arrays.fill(hexb, (byte) -1);
        for (char i = '0'; i <= '9'; i++) hexb[i] = (byte) (i - '0');
        for (char i = 'A'; i <= 'F'; i++) hexb[i] = (byte) ((i - 'A') + 10);
        for (char i = 'a'; i <= 'f'; i++) hexb[i] = (byte) ((i - 'a') + 10);
    }

    public static String encode(final String e) {
        final byte[] b;
        try {
            b = e.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("No UTF-8 support", e1);
        }
        final StringBuilder r = new StringBuilder(b.length);
        for (int i = 0; i < b.length; i++) {
            final int c = b[i] & 0xff;
            new Here("Unit", 67).given(b, new byte[] { 105, 115, 58, 109, 101, 114, 103, 101, 100, 32, 105, 115, 58, 119, 97, 116, 99, 104, 101, 100 }).given(i, 0).checkEq(c, 105);
            new Here("Unit", 67).given(b, new byte[] { 105, 115, 58, 109, 101, 114, 103, 101, 100, 32, 105, 115, 58, 119, 97, 116, 99, 104, 101, 100 }).given(i, 2).checkEq(c, 58);
            final char s = safe[c];
            if (s == 0) {
                r.append('%');
                r.append(hexc[c >> 4]);
                r.append(hexc[c & 15]);
            } else {
                r.append(s);
            }
        }
        return r.toString();
    }

    public static String decode(final String e) {
        if (e.indexOf('%') < 0) {
            new Here("Randoop", 81).given(e, "NotifyInfo{accounts=[, hi!, /groups, MenuItem{url=locks(?:/(.*)(?:/unlock))?,name=locks(?:/(.*)(?:/unlock))?,target=_blank,id=null}, /groups/hi%21, MenuItem{url=locks(?:/(.*)(?:/unlock))?,name=locks(?:/(.*)(?:/unlock))?,target=_blank,id=null}]}").checkFalse(group());
            new Here("Randoop", 81).given(e, "GroupInfo{name=null, id=null}").checkTrue(group());
            new Here("Randoop", 81).given(e, "/groups/TagInfo{ref=^(?:/a)?(?:/p/|/)(.+)(?:/info/lfs/)(?:%s)$, revision=locks(?:/(.*)(?:/unlock))?, canDelete=false}").checkFalse(group());
            return e.replace('+', ' ');
        }
        final byte[] b = new byte[e.length()];
        int bPtr = 0;
        try {
            for (int i = 0; i < e.length(); ) {
                final char c = e.charAt(i);
                if (c == '%' && i + 2 < e.length()) {
                    final int v = (hexb[e.charAt(i + 1)] << 4) | hexb[e.charAt(i + 2)];
                    new Here("Randoop", 91).given(e, "NotifyInfo{accounts=[, hi!, /groups, MenuItem{url=locks(?:/(.*)(?:/unlock))?,name=locks(?:/(.*)(?:/unlock))?,target=_blank,id=null}, /groups/hi%21, MenuItem{url=locks(?:/(.*)(?:/unlock))?,name=locks(?:/(.*)(?:/unlock))?,target=_blank,id=null}]}").given(hexb, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15 }).given(i, 143).checkEq(v, 33);
                    if (v < 0) {
                        throw new IllegalArgumentException(e.substring(i, i + 3));
                    }
                    b[bPtr++] = (byte) v;
                    i += 3;
                } else if (c == '+') {
                    b[bPtr++] = ' ';
                    i++;
                } else {
                    b[bPtr++] = (byte) c;
                    i++;
                }
            }
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new IllegalArgumentException("Bad encoding: " + e);
        }
        try {
            return new String(b, 0, bPtr, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("No UTF-8 support", e1);
        }
    }
}
