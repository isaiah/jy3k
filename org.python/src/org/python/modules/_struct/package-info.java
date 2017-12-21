 /** This module performs conversions between Python values and C
 * structs represented as Python strings.  It uses <i>format strings</i>
 * (explained below) as compact descriptions of the lay-out of the C
 * structs and the intended conversion to/from Python values.
 *
 * <P>
 * The module defines the following exception and functions:
 *
 * <P>
 * <dl><dt><b><tt>error</tt></b>
 * <dd>
 *   Exception raised on various occasions; argument is a string
 *   describing what is wrong.
 * </dl>
 *
 * <P>
 * <dl><dt><b><tt>pack</tt></b> (<var>fmt, v1, v2,  ...</var>)
 * <dd>
 *   Return a string containing the values
 *   <tt><i>v1</i>, <i>v2</i>,  ...</tt> packed according to the given
 *   format.  The arguments must match the values required by the format
 *   exactly.
 * </dl>
 *
 * <P>
 * <dl><dt><b><tt>unpack</tt>></b> (<var>fmt, string</var>)
 * <dd>
 *   Unpack the string (presumably packed by <tt>pack(<i>fmt</i>,
 *    ...)</tt>) according to the given format.  The result is a
 *   tuple even if it contains exactly one item.  The string must contain
 *   exactly the amount of data required by the format (i.e.
 *   <tt>len(<i>string</i>)</tt> must equal <tt>calcsize(<i>fmt</i>)</tt>).
 * </dl>
 *
 * <P>
 * <dl><dt><b><tt>calcsize</tt></b> (<var>fmt</var>)
 * <dd>
 *   Return the size of the struct (and hence of the string)
 *   corresponding to the given format.
 * </dl>
 *
 * <P>
 * Format characters have the following meaning; the conversion between
 * C and Python values should be obvious given their types:
 *
 * <P>
 * <table border align=center>
 *   <tr><th><b>Format</b></th>
 *       <th align=left><b>C Type</b></th>
 *       <th align=left><b>Python</b></th>
 *   <tr><td align=center><samp>x</samp></td>
 *       <td>pad byte</td>
 *       <td>no value</td>
 *   <tr><td align=center><samp>c</samp></td>
 *       <td><tt>char</tt></td>
 *       <td>string of length 1</td>
 *   <tr><td align=center><samp>b</samp></td>
 *       <td><tt>signed char</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>B</samp></td>
 *       <td><tt>unsigned char</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>h</samp></td>
 *       <td><tt>short</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>H</samp></td>
 *       <td><tt>unsigned short</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>i</samp></td>
 *       <td><tt>int</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>I</samp></td>
 *       <td><tt>unsigned int</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>size</samp></td>
 *       <td><tt>long</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>L</samp></td>
 *       <td><tt>unsigned long</tt></td>
 *       <td>integer</td>
 *   <tr><td align=center><samp>f</samp></td>
 *       <td><tt>float</tt></td>
 *       <td>float</td>
 *   <tr><td align=center><samp>d</samp></td>
 *       <td><tt>double</tt></td>
 *       <td>float</td>
 *   <tr><td align=center><samp>s</samp></td>
 *       <td><tt>char[]</tt></td>
 *       <td>string</td>
 *   <tr><td align=center><samp>p</samp></td>
 *       <td><tt>char[]</tt></td>
 *       <td>string</td>
 * </table>
 *
 * <P>
 * A format character may be preceded by an integral repeat count;
 * e.g. the format string <tt>'4h'</tt> means exactly the same as
 * <tt>'hhhh'</tt>.
 *
 * <P>
 * Whitespace characters between formats are ignored; a count and its
 * format must not contain whitespace though.
 *
 * <P>
 * For the "<tt>s</tt>" format character, the count is interpreted as the
 * size of the string, not a repeat count like for the other format
 * characters; e.g. <tt>'10s'</tt> means a single 10-byte string, while
 * <tt>'10c'</tt> means 10 characters.  For packing, the string is
 * truncated or padded with null bytes as appropriate to make it fit.
 * For unpacking, the resulting string always has exactly the specified
 * number of bytes.  As a special case, <tt>'0s'</tt> means a single, empty
 * string (while <tt>'0c'</tt> means 0 characters).
 *
 * <P>
 * The "<tt>p</tt>" format character can be used to encode a Pascal
 * string.  The first byte is the length of the stored string, with the
 * bytes of the string following.  If count is given, it is used as the
 * total number of bytes used, including the length byte.  If the string
 * passed in to <tt>pack()</tt> is too long, the stored representation
 * is truncated.  If the string is too short, padding is used to ensure
 * that exactly enough bytes are used to satisfy the count.
 *
 * <P>
 * For the "<tt>I</tt>" and "<tt>L</tt>" format characters, the return
 * value is a Python long integer.
 *
 * <P>
 * By default, C numbers are represented in the machine's native format
 * and byte order, and properly aligned by skipping pad bytes if
 * necessary (according to the rules used by the C compiler).
 *
 * <P>
 * Alternatively, the first character of the format string can be used to
 * indicate the byte order, size and alignment of the packed data,
 * according to the following table:
 *
 * <P>
 * <table border align=center>
 *
 *   <tr><th><b>Character</b></th>
 *       <th align=left><b>Byte order</b></th>
 *       <th align=left><b>Size and alignment</b></th>
 *   <tr><td align=center><samp>@</samp></td>
 *       <td>native</td>
 *       <td>native</td>
 *   <tr><td align=center><samp>=</samp></td>
 *       <td>native</td>
 *       <td>standard</td>
 *   <tr><td align=center><samp>&lt;</samp></td>
 *       <td>little-endian</td>
 *       <td>standard</td>
 *   <tr><td align=center><samp>&gt;</samp></td>
 *       <td>big-endian</td>
 *       <td>standard</td>
 *   <tr><td align=center><samp>!</samp></td>
 *       <td>network (= big-endian)</td>
 *       <td>standard</td>
 *
 * </table>
 *
 * <P>
 * If the first character is not one of these, "<tt>@</tt>" is assumed.
 *
 * <P>
 * Native byte order is big-endian or little-endian, depending on the
 * host system (e.g. Motorola and Sun are big-endian; Intel and DEC are
 * little-endian).
 *
 * <P>
 * Native size and alignment are defined as follows: <tt>short</tt> is
 * 2 bytes; <tt>int</tt> and <tt>long</tt> are 4 bytes; <tt>float</tt>
 * are 4 bytes and <tt>double</tt> are 8 bytes. Native byte order is
 * chosen as big-endian.
 *
 * <P>
 * Standard size and alignment are as follows: no alignment is required
 * for any type (so you have to use pad bytes); <tt>short</tt> is 2 bytes;
 * <tt>int</tt> and <tt>long</tt> are 4 bytes.  <tt>float</tt> and
 * <tt>double</tt> are 32-bit and 64-bit IEEE floating point numbers,
 * respectively.
 *
 * <P>
 * Note the difference between "<tt>@</tt>" and "<tt>=</tt>": both use
 * native byte order, but the size and alignment of the latter is
 * standardized.
 *
 * <P>
 * The form "<tt>!</tt>" is available for those poor souls who claim they
 * can't remember whether network byte order is big-endian or
 * little-endian.
 *
 * <P>
 * There is no way to indicate non-native byte order (i.e. force
 * byte-swapping); use the appropriate choice of "<tt>&lt;</tt>" or
 * "<tt>&gt;</tt>".
 *
 * <P>
 * Examples (all using native byte order, size and alignment, on a
 * big-endian machine):
 *
 * <P>
 * <dl><dd><pre>
 * &gt;&gt;&gt; from struct import *
 * &gt;&gt;&gt; pack('hhl', 1, 2, 3)
 * '\000\001\000\002\000\000\000\003'
 * &gt;&gt;&gt; unpack('hhl', '\000\001\000\002\000\000\000\003')
 * (1, 2, 3)
 * &gt;&gt;&gt; calcsize('hhl')
 * 8
 * &gt;&gt;&gt;
 * </pre></dl>
 *
 * <P>
 * Hint: to align the end of a structure to the alignment requirement of
 * a particular type, end the format with the code for that type with a
 * repeat count of zero, e.g. the format <tt>'llh0l'</tt> specifies two
 * pad bytes at the end, assuming longs are aligned on 4-byte boundaries.
 * This only works when native size and alignment are in effect;
 * standard size and alignment does not enforce any alignment.
 *
 * For the complete documentation on the struct module, please see the
 * "Python Library Reference"
 * <p><hr><p>
 *
 * The module is based on the original structmodule.c except that all
 * mistakes and errors are my own. Original author unknown.
 * <p>
 * @author Finn Bock, bckfnn@pipmail.dknet.dk
 * @version struct.java,v 1.6 1999/04/17 12:04:34 fb Exp
 */

package org.python.modules._struct;