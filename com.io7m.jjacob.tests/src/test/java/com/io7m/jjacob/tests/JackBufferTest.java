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

import com.io7m.jjacob.api.JackAbstractBuffer;
import com.io7m.jjacob.api.JackBufferType;

public final class JackBufferTest extends JackBufferContract
{
  @Override
  protected JackBufferType buffer(
    final int frames_count,
    final int frames_size)
  {
    return new JackAbstractBuffer(frames_count, frames_size)
    {
      @Override
      protected void actualPutF(
        final long offset,
        final float value)
      {

      }

      @Override
      protected void actualPutArrayF(
        final long offset,
        final float[] values)
      {

      }

      @Override
      protected void actualPutI(
        final long offset,
        final int value)
      {

      }

      @Override
      protected void actualPutB(
        final long offset,
        final int value)
      {

      }

      @Override
      protected void actualPutArrayB(
        final long offset,
        final byte[] values)
      {

      }
    };
  }
}
