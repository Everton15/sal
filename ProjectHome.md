In many sensor network installations, middleware software has been written for specific sensing technologies and is therefore highly hardware-dependent. The task of integrating new hardware to a sensor network often involves modifying software in order to support new nodes/sensors. The Sensor Abstraction Layer (SAL) aims at simplifying sensor network access, control and management by:
  * Hiding hardware-dependent features away from users,
  * Providing a single programming interface to pilot sensors and manage the network
  * Automatically detecting and configuring new sensors, as far as the hardware allows it

To achieve these goals, our implementation of SAL relies on a plugin-based model, where plugins can be loaded & configured on the run to add support for new types of sensors, without having to re-program middleware software.

