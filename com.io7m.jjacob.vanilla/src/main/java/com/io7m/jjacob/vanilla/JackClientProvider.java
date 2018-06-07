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

import com.io7m.jjacob.api.JackAbstractBuffer;
import com.io7m.jjacob.api.JackBufferMIDIType;
import com.io7m.jjacob.api.JackBufferType;
import com.io7m.jjacob.api.JackClientActivateException;
import com.io7m.jjacob.api.JackClientCallbackRegistrationException;
import com.io7m.jjacob.api.JackClientClosedException;
import com.io7m.jjacob.api.JackClientConfiguration;
import com.io7m.jjacob.api.JackClientDeactivateException;
import com.io7m.jjacob.api.JackClientOpenException;
import com.io7m.jjacob.api.JackClientPortConnectionException;
import com.io7m.jjacob.api.JackClientPortRegistrationException;
import com.io7m.jjacob.api.JackClientPortSearchException;
import com.io7m.jjacob.api.JackClientPortTypeRegistryType;
import com.io7m.jjacob.api.JackClientProcessCallbackContextType;
import com.io7m.jjacob.api.JackClientProcessCallbackType;
import com.io7m.jjacob.api.JackClientProviderType;
import com.io7m.jjacob.api.JackClientType;
import com.io7m.jjacob.api.JackException;
import com.io7m.jjacob.api.JackPortFlag;
import com.io7m.jjacob.api.JackPortType;
import com.io7m.jjacob.api.JackStatusCode;
import com.io7m.jjacob.jnr.LibJackPortFlags;
import com.io7m.jjacob.jnr.LibJackStatus;
import com.io7m.jjacob.jnr.LibJackType;
import com.io7m.jjacob.porttype.api.JackPortTypeInformation;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_CAN_MONITOR;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_INPUT;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_OUTPUT;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_PHYSICAL;
import static com.io7m.jjacob.api.JackPortFlag.JACK_PORT_IS_TERMINAL;
import static com.io7m.jjacob.api.JackStatusCode.JACK_BACKEND_ERROR;
import static com.io7m.jjacob.api.JackStatusCode.JACK_CLIENT_ZOMBIE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_FAILURE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_INIT_FAILURE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_INVALID_OPTION;
import static com.io7m.jjacob.api.JackStatusCode.JACK_LOAD_FAILURE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_NAME_NOT_UNIQUE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_NO_SUCH_CLIENT;
import static com.io7m.jjacob.api.JackStatusCode.JACK_SERVER_ERROR;
import static com.io7m.jjacob.api.JackStatusCode.JACK_SERVER_FAILED;
import static com.io7m.jjacob.api.JackStatusCode.JACK_SERVER_STARTED;
import static com.io7m.jjacob.api.JackStatusCode.JACK_SHM_FAILURE;
import static com.io7m.jjacob.api.JackStatusCode.JACK_VERSION_ERROR;
import static com.io7m.jjacob.jnr.LibJackOptions.JackNoStartServer;
import static com.io7m.jjacob.jnr.LibJackOptions.JackServerName;
import static com.io7m.jjacob.jnr.LibJackOptions.JackUseExactName;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortCanMonitor;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsInput;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsOutput;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsPhysical;
import static com.io7m.jjacob.jnr.LibJackPortFlags.JackPortIsTerminal;
import static java.nio.charset.StandardCharsets.UTF_8;
import static jnr.constants.platform.Errno.EEXIST;

/**
 * The default client provider implementation.
 */

public final class JackClientProvider implements JackClientProviderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JackClientProvider.class);

  private final LibJackType libjack;
  private final JackClientPortTypeRegistryType types;

  private JackClientProvider(
    final LibJackType in_libjack,
    final JackClientPortTypeRegistryType in_types)
  {
    this.libjack = Objects.requireNonNull(in_libjack, "client");
    this.types = Objects.requireNonNull(in_types, "types");
  }

  /**
   * Create a new client provider.
   *
   * @param types   A registry of known port types
   * @param libjack A libjack implementation
   *
   * @return A new provider
   */

  public static JackClientProviderType create(
    final JackClientPortTypeRegistryType types,
    final LibJackType libjack)
  {
    Objects.requireNonNull(types, "types");
    Objects.requireNonNull(libjack, "client");
    return new JackClientProvider(libjack, types);
  }

  private static JackClientType fetchClientInformation(
    final Pointer client,
    final JackClientPortTypeRegistryType types,
    final LibJackType libjack)
  {
    final String client_real_name =
      libjack.jack_get_client_name(client);

    LOG.debug("opened client: {}", client_real_name);
    return new Client(libjack, types, client, client_real_name);
  }

  private static EnumSet<JackStatusCode> statusOf(final int status)
  {
    final EnumSet<JackStatusCode> result = EnumSet.noneOf(JackStatusCode.class);
    for (final LibJackStatus v : LibJackStatus.values()) {
      final int flag = v.intValue();
      if ((status & flag) == flag) {
        result.add(statusCodeOf(v));
      }
    }
    return result;
  }

  private static JackStatusCode statusCodeOf(
    final LibJackStatus v)
  {
    switch (v) {
      case JackFailure:
        return JACK_FAILURE;
      case JackInvalidOption:
        return JACK_INVALID_OPTION;
      case JackNameNotUnique:
        return JACK_NAME_NOT_UNIQUE;
      case JackServerStarted:
        return JACK_SERVER_STARTED;
      case JackServerFailed:
        return JACK_SERVER_FAILED;
      case JackServerError:
        return JACK_SERVER_ERROR;
      case JackNoSuchClient:
        return JACK_NO_SUCH_CLIENT;
      case JackLoadFailure:
        return JACK_LOAD_FAILURE;
      case JackInitFailure:
        return JACK_INIT_FAILURE;
      case JackShmFailure:
        return JACK_SHM_FAILURE;
      case JackVersionError:
        return JACK_VERSION_ERROR;
      case JackBackendError:
        return JACK_BACKEND_ERROR;
      case JackClientZombie:
        return JACK_CLIENT_ZOMBIE;
    }
    throw new UnreachableCodeException();
  }

  @Override
  public String toString()
  {
    return new StringBuilder(64)
      .append("[JackClientProvider ")
      .append(this.libjack)
      .append("]")
      .toString();
  }

  @Override
  public JackClientType openClient(
    final JackClientConfiguration configuration)
    throws JackException
  {
    Objects.requireNonNull(configuration, "configuration");

    int options = 0;
    final String client_name;
    final Optional<String> client_name_opt = configuration.clientName();
    if (client_name_opt.isPresent()) {
      client_name = client_name_opt.get();
      if (configuration.clientNameUseExact()) {
        options |= JackUseExactName.intValue();
      }
    } else {
      client_name = "jjacob";
    }

    LOG.debug("open client: {}", client_name);

    final Optional<String> server_name_opt = configuration.serverName();
    final String server_name;
    if (server_name_opt.isPresent()) {
      server_name = server_name_opt.get();
      options |= JackServerName.intValue();
    } else {
      server_name = null;
    }

    LOG.debug("server name: {}", server_name);

    if (!configuration.startServer()) {
      options |= JackNoStartServer.intValue();
    }

    final int[] status = new int[1];
    final Pointer client =
      this.libjack.jack_client_open(
        client_name,
        options,
        status,
        server_name);

    if (client.address() == 0L) {
      final EnumSet<JackStatusCode> status_of = statusOf(status[0]);
      throw new JackClientOpenException("Could not create client", status_of);
    }

    return fetchClientInformation(client, this.types, this.libjack);
  }

  private static final class Client implements JackClientType
  {
    private final LibJackType libjack;
    private final Pointer client;
    private final String client_real_name;
    private final JackClientProcessCallbackContext process_context;
    private final JackClientPortTypeRegistryType types;
    private volatile boolean active;
    private volatile boolean closed;
    private volatile JackClientProcessCallbackType process;

    Client(
      final LibJackType in_libjack,
      final JackClientPortTypeRegistryType in_types,
      final Pointer in_client,
      final String in_client_real_name)
    {
      this.libjack =
        Objects.requireNonNull(in_libjack, "client");
      this.types =
        Objects.requireNonNull(in_types, "types");
      this.client =
        Objects.requireNonNull(in_client, "client");
      this.client_real_name =
        Objects.requireNonNull(in_client_real_name, "client_real_name");

      this.closed = false;
      this.active = false;
      this.process_context = new JackClientProcessCallbackContext(this.libjack);
    }

    private static long flagsOf(
      final Set<JackPortFlag> options)
    {
      long flags = 0L;
      for (final JackPortFlag flag : options) {
        flags |= (long) flagOf(flag).intValue();
      }
      return flags;
    }

    private static LibJackPortFlags flagOf(
      final JackPortFlag flag)
    {
      switch (flag) {
        case JACK_PORT_IS_INPUT:
          return JackPortIsInput;
        case JACK_PORT_IS_OUTPUT:
          return JackPortIsOutput;
        case JACK_PORT_IS_PHYSICAL:
          return JackPortIsPhysical;
        case JACK_PORT_CAN_MONITOR:
          return JackPortCanMonitor;
        case JACK_PORT_IS_TERMINAL:
          return JackPortIsTerminal;
      }

      throw new UnreachableCodeException();
    }

    @Override
    public String name()
    {
      return this.client_real_name;
    }

    @Override
    public void activate()
      throws JackException
    {
      this.checkNotClosed();

      if (this.isActive()) {
        return;
      }

      final int r = this.libjack.jack_activate(this.client);
      if (r != 0) {
        throw new JackClientActivateException("Could not activate client");
      }
      this.active = true;
    }

    @Override
    public boolean isActive()
    {
      return this.active;
    }

    @Override
    public void deactivate()
      throws JackException
    {
      this.checkNotClosed();

      if (!this.isActive()) {
        return;
      }

      final int r = this.libjack.jack_deactivate(this.client);
      if (r != 0) {
        throw new JackClientDeactivateException("Could not deactivate client");
      }
      this.active = false;
    }

    @Override
    public int bufferSize()
      throws JackException
    {
      this.checkNotClosed();

      return this.libjack.jack_get_buffer_size(this.client);
    }

    @Override
    public int sampleRate()
      throws JackException
    {
      this.checkNotClosed();

      return this.libjack.jack_get_sample_rate(this.client);
    }

    @Override
    public float cpuLoad()
      throws JackException
    {
      this.checkNotClosed();

      return this.libjack.jack_cpu_load(this.client);
    }

    @Override
    public void setProcessCallback(
      final JackClientProcessCallbackType in_process)
      throws JackException
    {
      Objects.requireNonNull(in_process, "process");

      this.checkNotClosed();

      this.process = in_process;

      final int r =
        this.libjack.jack_set_process_callback(
          this.client,
          (frames, data) -> {
            try {
              this.process_context.buffer_size = frames;
              in_process.onProcess(this.process_context);
              return 0;
            } catch (final Exception e) {
              LOG.error("Process callback raised exception: ", e);
              return -1;
            }
          },
          null);

      if (r != 0) {
        this.process = null;
        throw new JackClientCallbackRegistrationException(
          "Unable to register process callback");
      }
    }

    @Override
    public JackPortType portRegister(
      final String name,
      final String type,
      final Set<JackPortFlag> options,
      final long buffer_size)
      throws JackException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(options, "options");

      this.checkNotClosed();

      final Optional<JackPortTypeInformation> type_info_opt =
        this.types.lookupByName(type);

      if (!type_info_opt.isPresent()) {
        throw new JackClientPortRegistrationException(
          "Unrecognized port type: " + type);
      }

      final Pointer pointer =
        this.libjack.jack_port_register(
          this.client,
          name,
          type,
          flagsOf(options),
          buffer_size);

      if (pointer.address() == 0L) {
        throw new JackClientPortRegistrationException(
          "Unable to register port");
      }

      return new Port(this, type_info_opt.get(), pointer);
    }

    @Override
    public List<String> portsList(
      final Optional<String> name_pattern,
      final Optional<String> type_pattern,
      final Set<JackPortFlag> flags)
      throws JackException
    {
      Objects.requireNonNull(name_pattern, "name_pattern");
      Objects.requireNonNull(type_pattern, "type_pattern");
      Objects.requireNonNull(flags, "flags");

      this.checkNotClosed();

      final long iflags = flagsOf(flags);
      final String name = name_pattern.orElse(null);
      final String type = type_pattern.orElse(null);

      final Runtime rt = Runtime.getSystemRuntime();

      final int max_size =
        this.libjack.jack_port_name_size();
      final Pointer ports =
        this.libjack.jack_get_ports(this.client, name, type, iflags);

      if (ports == null || ports.address() == 0L) {
        return List.of();
      }

      long offset = 0L;
      final ArrayList<String> results = new ArrayList<>(16);
      while (true) {
        final long port = ports.getAddress(offset);
        if (port == 0L) {
          break;
        }

        final Pointer name_ptr =
          ports.getPointer(offset);
        final String port_name =
          name_ptr.getString(0L, max_size, UTF_8);

        results.add(port_name);
        offset += (long) rt.addressSize();
      }

      this.libjack.jack_free(ports);
      return results;
    }

    @Override
    public boolean portsConnect(
      final String source_port,
      final String target_port)
      throws JackException
    {
      Objects.requireNonNull(source_port, "source_port");
      Objects.requireNonNull(target_port, "target_port");

      this.checkNotClosed();

      final int r =
        this.libjack.jack_connect(this.client, source_port, target_port);

      if (r == 0) {
        return true;
      }

      if (r == EEXIST.intValue()) {
        return false;
      }

      throw new JackClientPortConnectionException("Could not connect ports");
    }

    @Override
    public void portsDisconnect(
      final String source_port,
      final String target_port)
      throws JackException
    {
      Objects.requireNonNull(source_port, "source_port");
      Objects.requireNonNull(target_port, "target_port");

      this.checkNotClosed();

      final int r =
        this.libjack.jack_disconnect(this.client, source_port, target_port);

      if (r == 0) {
        return;
      }

      throw new JackClientPortConnectionException("Could not disconnect ports");
    }

    @Override
    public Optional<JackPortType> portByName(
      final String name)
      throws JackException
    {
      Objects.requireNonNull(name, "name");

      this.checkNotClosed();

      final Pointer port =
        this.libjack.jack_port_by_name(this.client, name);

      if (port == null || port.address() == 0L) {
        return Optional.empty();
      }

      final String type_name = this.libjack.jack_port_type(port);

      final Optional<JackPortTypeInformation> type_info_opt =
        this.types.lookupByName(type_name);

      if (!type_info_opt.isPresent()) {
        throw new JackClientPortSearchException(
          new StringBuilder(64)
            .append("Port ")
            .append(name)
            .append(" has unrecognized type: ")
            .append(type_name)
            .toString());
      }

      return Optional.of(new Port(this, type_info_opt.get(), port));
    }

    private void checkNotClosed()
      throws JackClientClosedException
    {
      if (this.isClosed()) {
        throw new JackClientClosedException("Client is closed");
      }
    }

    @Override
    public void close()
      throws JackException
    {
      this.checkNotClosed();

      try {
        LOG.debug("closing client: {}", this.name());
        this.libjack.jack_client_close(this.client);
      } finally {
        this.closed = true;
        this.active = false;
      }
    }

    @Override
    public boolean isClosed()
    {
      return this.closed;
    }

    @Override
    public String toString()
    {
      return new StringBuilder(32)
        .append("[JackClient ")
        .append(this.name())
        .append(" 0x")
        .append(Long.toUnsignedString(this.client.address(), 16))
        .append("]")
        .toString();
    }

    private static final class Port implements JackPortType
    {
      private final Client client;
      private final Pointer pointer;
      private final JackPortTypeInformation type;

      Port(
        final Client in_libjack,
        final JackPortTypeInformation in_type,
        final Pointer in_pointer)
      {
        this.client = Objects.requireNonNull(in_libjack, "client");
        this.type = Objects.requireNonNull(in_type, "type");
        this.pointer = Objects.requireNonNull(in_pointer, "pointer");
      }

      private static JackPortFlag flagOfLibJackFlag(
        final LibJackPortFlags flag)
      {
        switch (flag) {
          case JackPortIsInput:
            return JACK_PORT_IS_INPUT;
          case JackPortIsOutput:
            return JACK_PORT_IS_OUTPUT;
          case JackPortIsPhysical:
            return JACK_PORT_IS_PHYSICAL;
          case JackPortCanMonitor:
            return JACK_PORT_CAN_MONITOR;
          case JackPortIsTerminal:
            return JACK_PORT_IS_TERMINAL;
        }
        throw new UnreachableCodeException();
      }

      @Override
      public JackClientType connection()
      {
        return this.client;
      }

      @Override
      public String shortName()
        throws JackException
      {
        this.client.checkNotClosed();
        return this.client.libjack.jack_port_short_name(this.pointer);
      }

      @Override
      public String name()
        throws JackException
      {
        this.client.checkNotClosed();
        return this.client.libjack.jack_port_name(this.pointer);
      }

      @Override
      public String typeName()
        throws JackException
      {
        this.client.checkNotClosed();
        return this.type.name();
      }

      @Override
      public JackPortTypeInformation type()
        throws JackException
      {
        this.client.checkNotClosed();
        return this.type;
      }

      @Override
      public Set<JackPortFlag> flags()
        throws JackException
      {
        this.client.checkNotClosed();

        final int raw_flags = this.client.libjack.jack_port_flags(this.pointer);
        final EnumSet<JackPortFlag> result = EnumSet.noneOf(JackPortFlag.class);
        for (final LibJackPortFlags flag : LibJackPortFlags.values()) {
          final int flag_i = flag.intValue();
          if ((raw_flags & flag_i) == flag_i) {
            result.add(flagOfLibJackFlag(flag));
          }
        }
        return result;
      }

      @Override
      public boolean belongsTo(
        final JackClientType in_client)
        throws JackException
      {
        Objects.requireNonNull(in_client, "client");

        this.client.checkNotClosed();

        if (in_client instanceof Client) {
          final Client cc = (Client) in_client;
          return this.client.libjack.jack_port_is_mine(cc.client, this.pointer);
        }

        throw new IllegalArgumentException("Incompatible client class");
      }
    }
  }

  private static final class JackClientProcessCallbackContext
    implements JackClientProcessCallbackContextType
  {
    private final LibJackType libjack;
    private volatile int buffer_size;

    JackClientProcessCallbackContext(
      final LibJackType in_libjack)
    {
      this.libjack = Objects.requireNonNull(in_libjack, "client");
    }

    @Override
    public int bufferFrameCount()
    {
      return this.buffer_size;
    }

    @Override
    public JackBufferType portBuffer(
      final JackPortType port)
      throws JackException
    {
      Objects.requireNonNull(port, "port");

      if (port instanceof Client.Port) {
        final Client.Port pp = (Client.Port) port;
        pp.client.checkNotClosed();

        final Pointer buffer_ptr =
          this.libjack.jack_port_get_buffer(pp.pointer, this.buffer_size);
        if (buffer_ptr.address() == 0L) {
          throw new UnimplementedCodeException();
        }
        return new Buffer(
          this.buffer_size,
          pp.type.frameSizeBytes(),
          buffer_ptr);
      }

      throw new IllegalArgumentException("Incompatible port class");
    }

    @Override
    public JackBufferMIDIType portBufferMIDI(
      final JackPortType port)
      throws JackException
    {
      Objects.requireNonNull(port, "port");

      if (port instanceof Client.Port) {
        final Client.Port pp = (Client.Port) port;
        pp.client.checkNotClosed();

        if (!pp.type.isJackMIDI()) {
          throw new JackClientPortSearchException(
            new StringBuilder(64)
              .append(
                "Port is not of a type containing JACK MIDI events (is type '")
              .append(pp.type.name())
              .append("')")
              .toString());
        }

        final Pointer buffer_ptr =
          this.libjack.jack_port_get_buffer(pp.pointer, this.buffer_size);
        if (buffer_ptr.address() == 0L) {
          throw new UnimplementedCodeException();
        }

        final int event_count =
          this.libjack.jack_midi_get_event_count(buffer_ptr);
        final int lost_event_count =
          this.libjack.jack_midi_get_lost_event_count(buffer_ptr);

        return new MIDIBuffer(buffer_ptr, event_count, lost_event_count);
      }

      throw new IllegalArgumentException("Incompatible port class");
    }
  }

  private static final class MIDIBuffer implements JackBufferMIDIType
  {
    private final Pointer buffer_ptr;
    private final int event_count;
    private final int lost_event_count;

    MIDIBuffer(
      final Pointer in_buffer_ptr,
      final int in_event_count,
      final int in_lost_event_count)
    {
      this.buffer_ptr =
        Objects.requireNonNull(in_buffer_ptr, "buffer_ptr");
      this.event_count = in_event_count;
      this.lost_event_count = in_lost_event_count;
    }

    @Override
    public int eventCount()
    {
      return this.event_count;
    }

    @Override
    public int eventLostCount()
    {
      return this.lost_event_count;
    }
  }

  private static final class Buffer extends JackAbstractBuffer
  {
    private final Pointer buffer_ptr;

    Buffer(
      final int in_buffer_frame_count,
      final int in_buffer_frame_size,
      final Pointer in_buffer_ptr)
    {
      super(in_buffer_frame_count, in_buffer_frame_size);
      this.buffer_ptr =
        Objects.requireNonNull(in_buffer_ptr, "buffer_ptr");
    }

    @Override
    protected void actualPutF(
      final long offset,
      final float value)
    {
      this.buffer_ptr.putFloat(offset, value);
    }

    @Override
    protected void actualPutArrayF(
      final long offset,
      final float[] values)
    {
      this.buffer_ptr.put(offset, values, 0, values.length);
    }

    @Override
    protected void actualPutI(
      final long offset,
      final int value)
    {
      this.buffer_ptr.putInt(offset, value);
    }

    @Override
    protected void actualPutB(
      final long offset,
      final int value)
    {
      this.buffer_ptr.putByte(offset, (byte) (value & 0xff));
    }

    @Override
    protected void actualPutArrayB(
      final long offset,
      final byte[] values)
    {
      this.buffer_ptr.put(offset, values, 0, values.length);
    }

    @Override
    protected float actualGetF(final long offset)
    {
      return this.buffer_ptr.getFloat(offset);
    }

    @Override
    protected int actualGetI(final long offset)
    {
      return this.buffer_ptr.getInt(offset);
    }

    @Override
    protected int actualGetB(final long offset)
    {
      return (int) this.buffer_ptr.getByte(offset) & 0xff;
    }

    @Override
    protected void actualGetArrayF(
      final long offset,
      final float[] values,
      final int array_offset,
      final int length)
    {
      this.buffer_ptr.get(offset, values, array_offset, length);
    }

    @Override
    protected void actualGetArrayB(
      final long offset,
      final byte[] values,
      final int array_offset,
      final int length)
    {
      this.buffer_ptr.get(offset, values, array_offset, length);
    }
  }
}
