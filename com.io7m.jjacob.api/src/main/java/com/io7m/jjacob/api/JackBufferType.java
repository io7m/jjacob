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

/**
 * A buffer of samples. This is typically an abstraction over a section of
 * native memory. This native memory is typically mapped into the client's
 * address space from the server in order to provide low-latency inter-process
 * communication.
 */

public interface JackBufferType
{
  /**
   * @return The number of frames the buffer can hold
   */

  int frameCount();

  /**
   * @return The size in bytes of each frame
   */

  int frameSizeBytes();

  /**
   * @return The total size of the buffer in bytes
   */

  default long sizeBytes()
  {
    final long f_count = (long) this.frameCount();
    final long f_size = (long) this.frameSizeBytes();
    return Math.multiplyExact(f_count, f_size);
  }

  /**
   * Place a floating point value at the byte offset {@code index * 4} in the
   * buffer.
   *
   * @param index The index
   * @param value The value
   */

  void putF(
    int index,
    float value);

  /**
   * Place an integer value at the byte offset {@code index * 4} in the
   * buffer.
   *
   * @param index The index
   * @param value The value
   */

  void putI(
    int index,
    int value);

  /**
   * Place a byte value at the byte offset {@code offset} in the
   * buffer. The {@code value} parameter is of type {@code int} in order to
   * allow for the full range of unsigned byte values ({@code [0, 255]}).
   *
   * @param offset The offset
   * @param value The value
   */

  void putB(
    int offset,
    int value);

  /**
   * Place an array of floating point values at the byte offset {@code index * 4}
   * in the buffer.
   *
   * @param index  The index
   * @param values The values
   */

  void putArrayF(
    int index,
    float[] values);

  /**
   * Place an array of byte values at the byte offset {@code offset}
   * in the buffer.
   *
   * @param offset  The index
   * @param values The values
   */

  void putArrayB(
    int offset,
    byte[] values);
}
