/**
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *
 * (c) Daniel Lemire, http://lemire.me/en/
 */
package me.lemire.integercompression;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Implementation of variable-byte. Possibly inefficient.
 *
 * @author Daniel Lemire
 *
 */
public class VariableByte implements IntegerCODEC {
  @Override
public void compress(int[] in, IntWrapper inpos, int inlength, int[] out,
          IntWrapper outpos) {
    ByteBuffer buf = ByteBuffer.allocateDirect(inlength * 8);
    for (int k = inpos.get(); k < inpos.get() + inlength; ++k) {
      int val = in[k];
      do {
          int b = (val & 127);
          val >>>= 7;
          if (val != 0) {
              b |= 128;
          }
          buf.put((byte) b);
      } while (val != 0);
    }
    while (buf.position() % 4 != 0)
      buf.put((byte) 128);
    final int length = buf.position();
    buf.flip();
    IntBuffer ibuf = buf.asIntBuffer();
    ibuf.get(out, outpos.get(), length / 4);
    outpos.add(length / 4);
    inpos.add(inlength);
  }

  @Override
public void uncompress(int[] in, IntWrapper inpos, int inlength,
    int[] out, IntWrapper outpos) {
    int s = 0;
    int p = inpos.get();
    int finalp = inpos.get() + inlength;
    int tmpoutpos = outpos.get();
    for (int v = 0, shift = 0; p<finalp;) {
      int c = (byte)(in[p] >>> (24-s));
      s+=8; if(s == 32) {s = 0; p++;}
      v += ((c & 127) << shift);
      if ((c & 128) == 0) {
        out[tmpoutpos++] = v;
        v = 0;
        shift = 0;
      } else
        shift += 7;
    }
    outpos.set(tmpoutpos);
    inpos.add(inlength);
  }

  @Override
public String toString() {
    return this.getClass().getName();
  }

}
