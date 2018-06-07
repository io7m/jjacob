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

import com.io7m.jjacob.porttype.api.JackPortTypeInformation;
import com.io7m.jjacob.porttype.api.JackPortTypeInformationProviderType;
import net.jcip.annotations.GuardedBy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The default port typeName registry implementation.
 */

@Component(service = JackClientPortTypeRegistryType.class)
public final class JackPortTypeRegistry
  implements JackClientPortTypeRegistryType
{
  private final Object providers_lock;
  private final @GuardedBy("providers_lock")
  ArrayList<JackPortTypeInformationProviderType> providers;

  /**
   * Construct a registry.
   */

  public JackPortTypeRegistry()
  {
    this.providers_lock = new Object();
    this.providers = new ArrayList<>(8);
  }

  /**
   * Add a typeName provider.
   *
   * @param provider The provider
   */

  @Reference(
    cardinality = ReferenceCardinality.MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    policyOption = ReferencePolicyOption.GREEDY,
    unbind = "providerRemove")
  public void providerAdd(
    final JackPortTypeInformationProviderType provider)
  {
    synchronized (this.providers_lock) {
      this.providers.add(Objects.requireNonNull(provider, "provider"));
    }
  }

  /**
   * Remove a typeName provider.
   *
   * @param provider The provider
   */

  public void providerRemove(
    final JackPortTypeInformationProviderType provider)
  {
    synchronized (this.providers_lock) {
      this.providers.remove(provider);
    }
  }

  @Override
  public Optional<JackPortTypeInformation> lookupByName(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    for (final JackPortTypeInformationProviderType provider : this.providers) {
      final List<JackPortTypeInformation> types = provider.types();
      for (final JackPortTypeInformation type : types) {
        if (Objects.equals(type.name(), name)) {
          return Optional.of(type);
        }
      }
    }

    return Optional.empty();
  }
}
