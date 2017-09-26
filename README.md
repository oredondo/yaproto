# yaproto

This is a protocol development platform in JAVA.

Although it may be used for developing any application-level protocol defined by its state machine and its messages, 
it includes some special details that allow you to implement protocols located at the network level, such as routing 
protocols. To do this, it allows the use of unicast / multicast, and the use of raw sockets, even in JAVA 7 
(which doesn't provide native libraries for this).

As an example, the RIPv2 protocol is implemented. In the case of routing protocols, in addition, the Netlink protocol,
which allows to communicate with the core of a Linux system and establish new routes or to be observer is also 
partially implemented (in relation to route tables, network, ...) of route modifications introduced by other
routing protocols or the system administrator.

It is distributed with a state machine design, messages, actions, ie the components of the State design pattern,
from which to derive to generate new protocols.

As an example, RIPv2 (a state machine for the protocol and a state machine for each route) is implemented,
which uses raw socket and broadcast. It also implements the Netlink protocol (network, routing table) for 
communication with a Linux kernel.

More routing protocols, such as OSPFv2, are being developed.
