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
 * Option values.
 *
 * @see "jack_options_t"
 */

public enum LibJackOptions implements EnumMapper.IntegerEnum
{
  /**
   * "JackNullOption"
   */

  JackNullOption(0x00),

  /**
   * "JackNoStartServer"
   */

  JackNoStartServer(0x01),

  /**
   * "JackUseExactName"
   */

  JackUseExactName(0x02),

  /**
   * "JackServerName"
   */

  JackServerName(0x04),

  /**
   * "JackLoadName"
   */

  JackLoadName(0x08),

  /**
   * "JackLoadInit"
   */

  JackLoadInit(0x10),

  /**
   * "JackSessionID"
   */

  JackSessionID(0x20);

  private final int value;

  LibJackOptions(final int x)
  {
    this.value = x;
  }

  @Override
  public int intValue()
  {
    return this.value;
  }
}
