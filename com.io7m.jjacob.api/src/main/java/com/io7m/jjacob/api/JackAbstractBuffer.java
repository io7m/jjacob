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
   * Put a floating point value at the given byte offset.
   *
   * @param offset The byte offset
   * @param value  The value
   */

  protected abstract void actualPutF(
    long offset,
    float value);

  /**
   * Put a float array at the given byte offset.
   *
   * @param offset The byte offset
   * @param values The values
   */

  protected abstract void actualPutArrayF(
    long offset,
    float[] values);

  /**
   * Put an integer value at the given byte offset.
   *
   * @param offset The byte offset
   * @param value  The value
   */

  protected abstract void actualPutI(
    long offset,
    int value);

  /**
   * Put a byte value at the given byte offset.
   *
   * @param offset The byte offset
   * @param value  The value
   */

  protected abstract void actualPutB(
    long offset,
    int value);

  /**
   * Put a byte array at the given byte offset.
   *
   * @param offset The byte offset
   * @param values The values
   */

  protected abstract void actualPutArrayB(
    long offset,
    byte[] values);

  /**
   * Get a floating point value from the given byte offset.
   *
   * @param offset The byte offset
   */

  protected abstract float actualGetF(
    long offset);

  /**
   * Get an integer value from the given byte offset.
   *
   * @param offset The byte offset
   */

  protected abstract int actualGetI(
    long offset);

  /**
   * Get a byte value from the given byte offset.
   *
   * @param offset The byte offset
   */

  protected abstract int actualGetB(
    long offset);

  /**
   * Get an array of floats from the given byte offset.
   *
   * @param offset       The byte offset
   * @param values       The output array
   * @param array_offset The offset into the array to which to save values
   * @param length       The number of values to save
   */

  protected abstract void actualGetArrayF(
    long offset,
    float[] values,
    int array_offset,
    int length);

  /**
   * Get an array of bytes from the given byte offset.
   *
   * @param offset       The byte offset
   * @param values       The output array
   * @param array_offset The offset into the array to which to save values
   * @param length       The number of values to save
   */

  protected abstract void actualGetArrayB(
    long offset,
    byte[] values,
    int array_offset,
    int length);

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

    final long offset_end = 4L * (long) (index + Math.max(
      0,
      values.length - 1));
    this.checkBounds(index, offset_end);

    final long offset = 4L * (long) index;
    this.actualPutArrayF(offset, values);
  }

  @Override
  public final void putI(
    final int index,
    final int value)
  {
    final long offset = 4L * (long) index;
    this.checkBounds(index, offset);
    this.actualPutI(offset, value);
  }

  @Override
  public final void putB(
    final int offset,
    final int value)
  {
    this.checkBounds(offset, offset);
    this.actualPutB(offset, value);
  }

  @Override
  public final void putArrayB(
    final int offset,
    final byte[] values)
  {
    Objects.requireNonNull(values, "values");

    final long offset_end = (long) (offset + Math.max(0, values.length - 1));
    this.checkBounds(offset, offset_end);
    this.actualPutArrayB(offset, values);
  }

  @Override
  public final float getF(
    final int index)
  {
    final long offset = 4L * (long) index;
    this.checkBounds(index, offset);
    return this.actualGetF(offset);
  }

  @Override
  public final int getI(
    final int index)
  {
    final long offset = 4L * (long) index;
    this.checkBounds(index, offset);
    return this.actualGetI(offset);
  }

  @Override
  public final int getB(
    final int offset)
  {
    this.checkBounds(offset, offset);
    return this.actualGetB(offset);
  }

  @Override
  public void getArrayF(
    final int index,
    final float[] values,
    final int array_offset,
    final int length)
  {
    Objects.requireNonNull(values, "values");

    final long offset_end = 4L * (long) (index + Math.max(0, length - 1));
    this.checkBounds(index, offset_end);

    final long offset = 4L * (long) index;
    this.actualGetArrayF(offset, values, array_offset, length);
  }

  @Override
  public void getArrayB(
    final int offset,
    final byte[] values,
    final int array_offset,
    final int length)
  {
    Objects.requireNonNull(values, "values");

    final long offset_end = (long) (offset + Math.max(0, length - 1));
    this.checkBounds(offset, offset_end);
    this.actualGetArrayB(offset, values, array_offset, length);
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
