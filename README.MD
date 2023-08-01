# SerializationAgent

A Java Agent mitigation for unsafe Minecraft Mod Serialization.

Please see the blog post over here for further details on scope: https://blog.mmpa.info/posts/bleeding-pipe/

This Java Agent uses [SerializationIsBad](https://github.com/dogboy21/serializationisbad)'s metadata, which
employs the use of an allow list for known objects which mods deserialize.

### To use

Download the latest release available from GitHub:
https://github.com/CreeperHost/Log4jPatcher/releases

Add `-javaagent:SerializationAgent.jar` as a JVM argument.

### Metadata

By default, SerializationAgent will attempt to load metadata from the following places:

- 1, `serializationisbad.json` from the working directory of the Minecraft server.
  Creating this file will disable all later metadata locations.
- 2, The upstream metadata remote resource on [GitHub](https://github.com/dogboy21/serializationisbad/blob/master/serializationisbad.json).
  This can be disabled by appending `=offline` to the java agent argument.
- 3, The same upstream metadata resource that was baked in at build time.
