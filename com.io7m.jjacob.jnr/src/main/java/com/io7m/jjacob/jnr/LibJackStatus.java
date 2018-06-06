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

package com.io7m.jjacob.jnr;

import jnr.ffi.util.EnumMapper;

/**
 * JACK status values.
 */

public enum LibJackStatus implements EnumMapper.IntegerEnum
{
  /**
   * Overall operation failed.
   */
  JackFailure(0x01),

  /**
   * The operation contained an invalid or unsupported option.
   */
  JackInvalidOption(0x02),

  /**
   * The desired client name was not unique.  With the @ref
   * JackUseExactName option this situation is fatal.  Otherwise,
   * the name was modified by appending a dash and a two-digit
   * number in the range "-01" to "-99".  The
   * jack_get_client_name() function will return the exact string
   * that was used.  If the specified @a client_name plus these
   * extra characters would be too long, the open fails instead.
   */
  JackNameNotUnique(0x04),

  /**
   * The JACK server was started as a result of this operation.
   * Otherwise, it was running already.  In either case the caller
   * is now connected to jackd, so there is no race condition.
   * When the server shuts down, the client will find out.
   */
  JackServerStarted(0x08),

  /**
   * Unable to connect to the JACK server.
   */
  JackServerFailed(0x10),

  /**
   * Communication error with the JACK server.
   */
  JackServerError(0x20),

  /**
   * Requested client does not exist.
   */
  JackNoSuchClient(0x40),

  /**
   * Unable to load internal client
   */
  JackLoadFailure(0x80),

  /**
   * Unable to initialize client
   */
  JackInitFailure(0x100),

  /**
   * Unable to access shared memory
   */
  JackShmFailure(0x200),

  /**
   * Client's protocol version does not match
   */
  JackVersionError(0x400),

  /**
   * Backend error
   */
  JackBackendError(0x800),

  /**
   * Client zombified failure
   */
  JackClientZombie(0x1000);

  private final int value;

  LibJackStatus(final int i)
  {
    this.value = i;
  }

  @Override
  public int intValue()
  {
    return this.value;
  }
}
