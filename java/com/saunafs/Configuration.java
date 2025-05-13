package com.saunafs;

import static com.saunafs.common.io.IoFactories.address;
import static com.saunafs.common.io.IoFactories.socketAddress;

import java.net.InetSocketAddress;

public class Configuration {
  public static final InetSocketAddress aNeutrinoLocalWifi = socketAddress(
      address("192.168.168.160"), 9422);
  public static final InetSocketAddress aNeutrinoLocalLan = socketAddress(
      address("192.168.168.96"), 9422);
  public static final InetSocketAddress aNeutrinoGlobal = socketAddress(
      address("cajar.ddnnss.eu"), 9422);
}
