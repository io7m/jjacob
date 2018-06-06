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

/**
 * Functions regarding port types.
 */

public final class LibJackPorts
{
  private LibJackPorts()
  {

  }

  /**
   * @return The default audio type
   *
   * @see "JACK_DEFAULT_AUDIO_TYPE"
   */

  public static String defaultAudioType()
  {
    return "32 bit float mono audio";
  }

  /**
   * @return The default midi type
   *
   * @see "JACK_DEFAULT_MIDI_TYPE"
   */

  public static String defaultMidiType()
  {
    return "8 bit raw midi";
  }
}
