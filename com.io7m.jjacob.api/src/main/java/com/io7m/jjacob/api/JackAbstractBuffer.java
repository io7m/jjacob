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

package com.io7m.jjacob.api;

import java.util.Objects;

/**
 * An abstract implementation of the {@link JackBufferType} interface.
 */

public abstract class JackAbstractBuffer implements JackBufferType
{
  private final int buffer_frames;
  private final int buffer_frame_size;

  @Override
  public final int frameCount()
  {
    return this.buffer_frames;
  }

  @Override
  public final int frameSizeBytes()
  {
    return this.buffer_frame_size;
  }

  /**
   * Construct a buffer.
   *
   * @param in_buffer_frames The size of the buffer in frames
   */

  protected JackAbstractBuffer(
    final int in_buffer_frames,
    final int in_buffer_frame_size)
  {
    this.buffer_frames = in_buffer_frames;
    this.buffer_frame_size = in_buffer_frame_size;
  }

  /**
   * Put a floating point value at the given byte offset.
   *
   * @param offset The byte offset
   * @param value  The value
   */

  protected abstract void actualPutF(
    long offset,
    float value);

  @Override
  public final void putF(
    final int index,
    final float value)
  {
    final long offset = 4L * (long) index;
    this.checkBounds(index, offset);
    this.actualPutF(offset, value);
  }

  @Override
  public final void putArrayF(
    final int index,
    final float[] values)
  {
    Objects.requireNonNull(values, "values");

    final long offset_end = 4L * (long) (index + Math.max(0, values.length - 1));
    this.checkBounds(index, offset_end);

    long offset = 4L * (long) index;
    for (int e_index = 0; e_index < values.length; ++e_index) {
      final float x = values[e_index];
      this.actualPutF(offset, x);
      offset += 4L;
    }
  }

  private void checkBounds(
    final int index,
    final long offset_end)
  {
    if (index < 0 || offset_end >= this.sizeBytes()) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }
}
