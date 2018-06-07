/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jjacob.tests;

import com.io7m.jjacob.api.JackBufferType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class JackBufferContract
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  protected abstract JackBufferType buffer(
    int frames_count,
    int frames_size);

  @Test
  public final void testPutOKF()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putF(index, (float) index);
    }
  }

  @Test
  public final void testPutOKI()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putI(index, index);
    }
  }

  @Test
  public final void testPutOKB()
  {
    final JackBufferType buffer = this.buffer(128, 1);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putB(index, index);
    }
  }

  @Test
  public final void testPutOKArrayF()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putArrayF(index, new float[] {(float) index});
    }
  }

  @Test
  public final void testPutOKArrayB()
  {
    final JackBufferType buffer = this.buffer(128, 1);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putArrayB(index, new byte[] {(byte) index});
    }
  }

  @Test
  public final void testPutOverflow0()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putF(128, 23.0f);
  }

  @Test
  public final void testPutOverflow1()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putF(-1, 23.0f);
  }

  @Test
  public final void testPutOverflow2()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
  }

  @Test
  public final void testPutOverflow3()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayF(-1, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
  }

  @Test
  public final void testPutOverflow4()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayB(-1, new byte[]{0, 0, 0, 0});
  }

  @Test
  public final void testPutOverflow5()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putB(-1, 0);
  }

  @Test
  public final void testPutOverflow6()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putB(4, 0);
  }

  @Test
  public final void testPutOverflow7()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
  }



















  @Test
  public final void testGetOKF()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.getF(index);
    }
  }

  @Test
  public final void testGetOKI()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.getI(index);
    }
  }

  @Test
  public final void testGetOKB()
  {
    final JackBufferType buffer = this.buffer(128, 1);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.getB(index);
    }
  }

  @Test
  public final void testGetOKArrayF()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.getArrayF(index, new float[] {(float) index});
    }
  }

  @Test
  public final void testGetOKArrayB()
  {
    final JackBufferType buffer = this.buffer(128, 1);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.getArrayB(index, new byte[] {(byte) index});
    }
  }

  @Test
  public final void testGetOverflow0()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getF(128);
  }

  @Test
  public final void testGetOverflow1()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getF(-1);
  }

  @Test
  public final void testGetOverflow2()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getArrayF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
  }

  @Test
  public final void testGetOverflow3()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getArrayF(-1, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
  }

  @Test
  public final void testGetOverflow4()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getArrayB(-1, new byte[]{0, 0, 0, 0});
  }

  @Test
  public final void testGetOverflow5()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getB(-1);
  }

  @Test
  public final void testGetOverflow6()
  {
    final JackBufferType buffer = this.buffer(4, 1);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getB(4);
  }

  @Test
  public final void testGetOverflow7()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.getArrayF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
  }
}
