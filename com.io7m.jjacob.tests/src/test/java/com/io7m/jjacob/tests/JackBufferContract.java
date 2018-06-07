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
  public void testOK()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putF(index, (float) index);
    }
  }

  @Test
  public void testOKArray()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    for (int index = 0; index < buffer.frameCount(); ++index) {
      buffer.putArrayF(index, new float[] {(float) index});
    }
  }

  @Test
  public void testOverflow0()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putF(128, 23.0f);
  }

  @Test
  public void testOverflow1()
  {
    final JackBufferType buffer = this.buffer(128, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putF(-1, 23.0f);
  }

  @Test
  public void testOverflow2()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayF(0, new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f});
  }

  @Test
  public void testOverflow3()
  {
    final JackBufferType buffer = this.buffer(4, 4);

    this.expected.expect(ArrayIndexOutOfBoundsException.class);
    buffer.putArrayF(-1, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
  }
}
