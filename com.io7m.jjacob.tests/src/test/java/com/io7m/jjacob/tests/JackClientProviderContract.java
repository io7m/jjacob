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

import com.io7m.jjacob.api.JackClientActivateException;
import com.io7m.jjacob.api.JackClientCallbackRegistrationException;
import com.io7m.jjacob.api.JackClientClosedException;
import com.io7m.jjacob.api.JackClientConfiguration;
import com.io7m.jjacob.api.JackClientDeactivateException;
import com.io7m.jjacob.api.JackClientOpenException;
import com.io7m.jjacob.api.JackClientPortConnectionException;
import com.io7m.jjacob.api.JackClientPortRegistrationException;
import com.io7m.jjacob.api.JackClientProviderType;
import com.io7m.jjacob.api.JackClientType;
import com.io7m.jjacob.api.JackPortType;
import com.io7m.jjacob.jnr.LibJackOptions;
import com.io7m.jjacob.jnr.LibJackPorts;
import com.io7m.jjacob.jnr.LibJackType;
import jnr.constants.platform.Errno;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_OUTPUT;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsOutput;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class JackClientProviderContract
{
  protected abstract JackClientProviderType clientProvider(
    LibJackType libjack);

  @Rule public final ExpectedException expected = ExpectedException.none();

  /**
   * Failing to open a connection raises an exception.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testOpenFailure()
    throws Exception
  {
    final LibJackUnsupported libjack =
      new LibJackUnsupported()
      {
        @Override
        public Pointer jack_client_open(
          final String name,
          final int options,
          final int[] status,
          final String server_name)
        {
          return Pointer.wrap(Runtime.getSystemRuntime(), 0L);
        }
      };

    final JackClientProviderType provider = this.clientProvider(libjack);
    System.out.println("Provider: " + provider);

    this.expected.expect(JackClientOpenException.class);
    provider.openClient(JackClientConfiguration.builder().build());
  }

  /**
   * Opening and closing a client works correctly when the server sends the
   * right responses.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testOpenCloseOK0()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient();

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      System.out.println("Client: " + client);
      Assert.assertFalse("Not closed", client.isClosed());
      Assert.assertEquals("test", client.name());
    }
  }

  /**
   * Opening and closing a client works correctly when the server sends the
   * right responses.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testOpenCloseOK1()
    throws Exception
  {
    final LibJackUnsupported libjack =
      new LibJackUnsupported()
      {
        @Override
        public String jack_get_client_name(
          final Pointer client)
        {
          return "test";
        }

        @Override
        public int jack_client_close(
          final Pointer client)
        {
          return 0;
        }

        @Override
        public Pointer jack_client_open(
          final String name,
          final int options,
          final int[] status,
          final String server_name)
        {
          Assert.assertEquals("test", name);

          Assert.assertEquals(
            "Server start flag is set",
            0,
            (options & LibJackOptions.JackNoStartServer.intValue()));

          Assert.assertEquals("hello", server_name);
          return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
        }
      };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .setClientNameUseExact(true)
               .setServerName("hello")
               .setStartServer(true)
               .build())) {

      System.out.println("Client: " + client);
      Assert.assertFalse("Not closed", client.isClosed());
      Assert.assertEquals("test", client.name());
    }
  }

  /**
   * Activating a closed client fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testActivateClosed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient();

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.close();
      this.expected.expect(JackClientClosedException.class);
      client.activate();
    }
  }

  /**
   * Activation failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testActivateFailed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_activate(final Pointer client)
      {
        return -1;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      this.expected.expect(JackClientActivateException.class);
      client.activate();
    }
  }

  /**
   * Activation works and is idempotent.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testActivate()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();
      Assert.assertTrue("Activated", client.isActive());
      client.activate();
      Assert.assertTrue("Activated", client.isActive());
    }
  }

  /**
   * Deactivation works and is idempotent.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testDeactivate()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_activate(
        final Pointer client)
      {
        return 0;
      }

      @Override
      public int jack_deactivate(
        final Pointer client)
      {
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();
      Assert.assertTrue("Activated", client.isActive());
      client.deactivate();
      Assert.assertTrue("Deactivated", !client.isActive());
      client.deactivate();
      Assert.assertTrue("Deactivated", !client.isActive());
    }
  }

  /**
   * Deactivation failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testDeactivateFailure()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_activate(
        final Pointer client)
      {
        return 0;
      }

      @Override
      public int jack_deactivate(
        final Pointer client)
      {
        return -1;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();
      Assert.assertTrue("Activated", client.isActive());

      this.expected.expect(JackClientDeactivateException.class);
      client.deactivate();
    }
  }

  /**
   * Fetching the sample rate via a closed client fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testSampleRateClosed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient();

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.close();
      this.expected.expect(JackClientClosedException.class);
      client.sampleRate();
    }
  }

  /**
   * Fetching the buffer size via a closed client fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testBufferSizeClosed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient();

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.close();
      this.expected.expect(JackClientClosedException.class);
      client.bufferSize();
    }
  }

  /**
   * Fetching the buffer size via a closed client fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testCPULoadClosed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient();

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.close();
      this.expected.expect(JackClientClosedException.class);
      client.cpuLoad();
    }
  }

  /**
   * Process callback registration failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testSetProcessCallbackFailure()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_set_process_callback(
        final Pointer client,
        final ProcessCallbackType process,
        final Pointer data)
      {
        return -1;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      this.expected.expect(JackClientCallbackRegistrationException.class);
      client.setProcessCallback(context -> {

      });
    }
  }

  /**
   * Process callback registration works and is idempotent.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testSetProcessCallback()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_set_process_callback(
        final Pointer client,
        final ProcessCallbackType process,
        final Pointer data)
      {
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.setProcessCallback(context -> {

      });
      client.setProcessCallback(context -> {

      });
    }
  }

  /**
   * Buffer size retrieval works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testBufferSize()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_get_buffer_size(final Pointer client)
      {
        return 1024;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      Assert.assertEquals(1024L, (long) client.bufferSize());
    }
  }

  /**
   * Sample rate retrieval works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testSampleRate()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_get_sample_rate(final Pointer client)
      {
        return 44100;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      Assert.assertEquals(44100L, (long) client.sampleRate());
    }
  }

  /**
   * CPU load retrieval works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testCPULoad()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public float jack_cpu_load(final Pointer client)
      {
        return 10.0f;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      Assert.assertEquals(10.0, (double) client.cpuLoad(), 0.0);
    }
  }

  /**
   * Listing ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testListPortsInput()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_port_name_size()
      {
        return 1024;
      }

      @Override
      public void jack_free(
        final Pointer pointer)
      {

      }

      @Override
      public Pointer jack_get_ports(
        final Pointer client,
        final String port_name_pattern,
        final String type_name_pattern,
        final long flags)
      {
        final Runtime rt = Runtime.getSystemRuntime();
        final Pointer p = Memory.allocateDirect(rt, 4 * rt.addressSize());

        final Pointer s0 = Memory.allocateDirect(rt, 4);
        s0.putString(0L, "AAA", 3, UTF_8);
        final Pointer s1 = Memory.allocateDirect(rt, 4);
        s1.putString(0L, "BBB", 3, UTF_8);
        final Pointer s2 = Memory.allocateDirect(rt, 4);
        s2.putString(0L, "CCC", 3, UTF_8);

        p.putPointer(0L, s0);
        p.putPointer(1L * (long) rt.addressSize(), s1);
        p.putPointer(2L * (long) rt.addressSize(), s2);
        p.putAddress(3L * (long) rt.addressSize(), 0L);
        return p;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      {
        final List<String> names = client.portsListAllInputs();
        Assert.assertEquals(3L, (long) names.size());
        Assert.assertEquals("AAA", names.get(0));
        Assert.assertEquals("BBB", names.get(1));
        Assert.assertEquals("CCC", names.get(2));
      }

      {
        final List<String> names = client.portsListAllInputs();
        Assert.assertEquals(3L, (long) names.size());
        Assert.assertEquals("AAA", names.get(0));
        Assert.assertEquals("BBB", names.get(1));
        Assert.assertEquals("CCC", names.get(2));
      }
    }
  }

  /**
   * Listing ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testListPortsOutput()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_port_name_size()
      {
        return 1024;
      }

      @Override
      public void jack_free(
        final Pointer pointer)
      {

      }

      @Override
      public Pointer jack_get_ports(
        final Pointer client,
        final String port_name_pattern,
        final String type_name_pattern,
        final long flags)
      {
        final Runtime rt = Runtime.getSystemRuntime();
        final Pointer p = Memory.allocateDirect(rt, 4 * rt.addressSize());

        final Pointer s0 = Memory.allocateDirect(rt, 4);
        s0.putString(0L, "AAA", 3, UTF_8);
        final Pointer s1 = Memory.allocateDirect(rt, 4);
        s1.putString(0L, "BBB", 3, UTF_8);
        final Pointer s2 = Memory.allocateDirect(rt, 4);
        s2.putString(0L, "CCC", 3, UTF_8);

        p.putPointer(0L, s0);
        p.putPointer(1L * (long) rt.addressSize(), s1);
        p.putPointer(2L * (long) rt.addressSize(), s2);
        p.putAddress(3L * (long) rt.addressSize(), 0L);
        return p;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(3L, (long) names.size());
        Assert.assertEquals("AAA", names.get(0));
        Assert.assertEquals("BBB", names.get(1));
        Assert.assertEquals("CCC", names.get(2));
      }

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(3L, (long) names.size());
        Assert.assertEquals("AAA", names.get(0));
        Assert.assertEquals("BBB", names.get(1));
        Assert.assertEquals("CCC", names.get(2));
      }
    }
  }

  /**
   * Listing ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testListPortsNull0()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_port_name_size()
      {
        return 1024;
      }

      @Override
      public void jack_free(
        final Pointer pointer)
      {

      }

      @Override
      public Pointer jack_get_ports(
        final Pointer client,
        final String port_name_pattern,
        final String type_name_pattern,
        final long flags)
      {
        return Pointer.wrap(Runtime.getSystemRuntime(), 0L);
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(0L, (long) names.size());
      }

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(0L, (long) names.size());
      }
    }
  }

  /**
   * Listing ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testListPortsNull1()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public int jack_port_name_size()
      {
        return 1024;
      }

      @Override
      public void jack_free(
        final Pointer pointer)
      {

      }

      @Override
      public Pointer jack_get_ports(
        final Pointer client,
        final String port_name_pattern,
        final String type_name_pattern,
        final long flags)
      {
        return null;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(0L, (long) names.size());
      }

      {
        final List<String> names = client.portsListAllOutputs();
        Assert.assertEquals(0L, (long) names.size());
      }
    }
  }

  /**
   * Looking up ports by name works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortByName()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_by_name(
        final Pointer client,
        final String name)
      {
        Assert.assertEquals("xyz", name);
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      final Optional<JackPortType> opt = client.portByName("xyz");
      Assert.assertTrue("port exists", opt.isPresent());
    }
  }

  /**
   * Looking up ports by name works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortByNameNonexistent0()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_by_name(
        final Pointer client,
        final String name)
      {
        Assert.assertEquals("xyz", name);
        return Pointer.wrap(Runtime.getSystemRuntime(), 0L);
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      final Optional<JackPortType> opt = client.portByName("xyz");
      Assert.assertFalse("port exists", opt.isPresent());
    }
  }

  /**
   * Looking up ports by name works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortByNameNonexistent1()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_by_name(
        final Pointer client,
        final String name)
      {
        Assert.assertEquals("xyz", name);
        return null;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      final Optional<JackPortType> opt = client.portByName("xyz");
      Assert.assertFalse("port exists", opt.isPresent());
    }
  }

  /**
   * Looking up ports by name works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortByNameClosed()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {

    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.close();
      this.expected.expect(JackClientClosedException.class);
      client.portByName("xyz");
    }
  }

  /**
   * Port registration works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortRegister()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }

      @Override
      public String jack_port_name(final Pointer port)
      {
        return "test:out_L";
      }

      @Override
      public String jack_port_short_name(final Pointer pointer)
      {
        return "out_L";
      }

      @Override
      public String jack_port_type(final Pointer pointer)
      {
        return LibJackPorts.defaultAudioType();
      }

      @Override
      public int jack_port_flags(final Pointer pointer)
      {
        return JackPortIsOutput.intValue();
      }

      @Override
      public boolean jack_port_is_mine(
        final Pointer client,
        final Pointer pointer)
      {
        return true;
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();

      final JackPortType port =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));

      Assert.assertEquals(client, port.connection());
      Assert.assertEquals("out_L", port.shortName());
      Assert.assertEquals("test:out_L", port.name());
      Assert.assertEquals(EnumSet.of(JACK_PORT_IS_OUTPUT), port.flags());
      Assert.assertEquals(LibJackPorts.defaultAudioType(), port.type());
      Assert.assertTrue("Port belongs to client", port.belongsTo(client));
    }
  }

  /**
   * Port registration failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortRegisterFailure()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Pointer.wrap(Runtime.getSystemRuntime(), 0L);
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();
      this.expected.expect(JackClientPortRegistrationException.class);
      client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
    }
  }

  /**
   * Connecting ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortConnection()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }

      private boolean connected;

      @Override
      public int jack_connect(
        final Pointer client,
        final String source_port,
        final String target_port)
      {
        Assert.assertEquals("test:out_L", source_port);
        Assert.assertEquals("test:out_R", target_port);

        if (this.connected) {
          return Errno.EEXIST.intValue();
        }
        this.connected = true;
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();

      final JackPortType port0 =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port1 =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      final boolean c0 =
        client.portsConnect("test:out_L", "test:out_R");
      Assert.assertTrue("connected", c0);

      final boolean c1 =
        client.portsConnect("test:out_L", "test:out_R");
      Assert.assertFalse("not connected", c1);
    }
  }

  /**
   * Connection failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortConnectionFailures()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }

      @Override
      public int jack_connect(
        final Pointer client,
        final String source_port,
        final String target_port)
      {
        Assert.assertEquals("test:out_L", source_port);
        Assert.assertEquals("test:out_R", target_port);
        return -1;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();

      final JackPortType port0 =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port1 =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      this.expected.expect(JackClientPortConnectionException.class);
      client.portsConnect("test:out_L", "test:out_R");
    }
  }

  /**
   * Disconnection failures raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortDisconnectionFailures()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }

      @Override
      public int jack_disconnect(
        final Pointer client,
        final String source_port,
        final String target_port)
      {
        Assert.assertEquals("test:out_L", source_port);
        Assert.assertEquals("test:out_R", target_port);
        return -1;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();

      final JackPortType port0 =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port1 =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      this.expected.expect(JackClientPortConnectionException.class);
      client.portsDisconnect("test:out_L", "test:out_R");
    }
  }

  /**
   * Disconnecting ports works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPortDisconnection()
    throws Exception
  {
    final LibJackUnsupported libjack = new LibJackWithTestClient()
    {
      @Override
      public Pointer jack_port_register(
        final Pointer client,
        final String port_name,
        final String port_type,
        final long flags,
        final long buffer_size)
      {
        return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
      }

      @Override
      public int jack_activate(final Pointer client)
      {
        return 0;
      }

      private boolean connected;

      @Override
      public int jack_connect(
        final Pointer client,
        final String source_port,
        final String target_port)
      {
        Assert.assertEquals("test:out_L", source_port);
        Assert.assertEquals("test:out_R", target_port);

        if (this.connected) {
          return Errno.EEXIST.intValue();
        }
        this.connected = true;
        return 0;
      }

      @Override
      public int jack_disconnect(
        final Pointer client,
        final String source_port,
        final String target_port)
      {
        Assert.assertEquals("test:out_L", source_port);
        Assert.assertEquals("test:out_R", target_port);
        return 0;
      }
    };

    final JackClientProviderType provider = this.clientProvider(libjack);

    try (final JackClientType client =
           provider.openClient(
             JackClientConfiguration
               .builder()
               .setClientName("test")
               .build())) {

      client.activate();

      final JackPortType port0 =
        client.portRegister("out_L", EnumSet.of(JACK_PORT_IS_OUTPUT));
      final JackPortType port1 =
        client.portRegister("out_R", EnumSet.of(JACK_PORT_IS_OUTPUT));

      final boolean c0 =
        client.portsConnect("test:out_L", "test:out_R");
      Assert.assertTrue("connected", c0);

      client.portsDisconnect("test:out_L", "test:out_R");
    }
  }

  private static class LibJackWithTestClient extends LibJackUnsupported
  {
    @Override
    public final Pointer jack_client_open(
      final String name,
      final int options,
      final int[] status,
      final String server_name)
    {
      return Memory.allocateDirect(Runtime.getSystemRuntime(), 4);
    }

    @Override
    public final String jack_get_client_name(
      final Pointer client)
    {
      return "test";
    }

    @Override
    public final int jack_client_close(
      final Pointer client)
    {
      return 0;
    }
  }
}
