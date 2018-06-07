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

package com.io7m.jjacob.vanilla;

import com.io7m.jjacob.jnr.LibJackPorts;
import com.io7m.jjacob.porttype.api.JackPortTypeInformation;
import com.io7m.jjacob.porttype.api.JackPortTypeInformationProviderType;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default port types.
 */

@Component(service = JackPortTypeInformationProviderType.class)
public final class JackPortTypesDefault
  implements JackPortTypeInformationProviderType
{
  private final List<JackPortTypeInformation> types_read;

  /**
   * Construct a provider.
   */

  public JackPortTypesDefault()
  {
    final ArrayList<JackPortTypeInformation> types =
      new ArrayList<>(1);

    this.types_read = Collections.unmodifiableList(types);
    types.add(
      JackPortTypeInformation.of(LibJackPorts.defaultAudioType(), 4));
  }

  @Override
  public List<JackPortTypeInformation> types()
  {
    return this.types_read;
  }
}
