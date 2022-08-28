[![Jitpack Release](https://jitpack.io/v/Strikeless/MCBootstrapHook.svg)](https://jitpack.io/#Strikeless/MCBootstrapHook)
![Supported Versions](https://img.shields.io/badge/Supported%20Versions-1.8--1.19-blue)

# MCBootstrapHook
Inject to a Minecraft server's Netty bootstrap with ease. Works on all versions from 1.8 to 1.19!

## What is it?
MCBootstrapHook or simply BootstrapHook helps Spigot plugins inject a Netty ChannelInitializer to a client's pipeline almost instantly upon connecting.
This allows you to get a hold of the Netty pipeline at a very early stage, which in turn allows you to manage packet traffic sent by and/or to the client.
BootstrapHook also focuses on being a rather simple utility, which in turn allows great flexibility and performance.

## What is it not?
* A packet system, MCBootstrapHook is a utility that can be used to make a packet system. If you're looking for a packet system, consider [PacketEvents](https://github.com/retrooper/packetevents) (unrelated to MCBootstrapHook).
* An intended standalone plugin, while MCBootstrapHook can work as one, it won't have a use.

## How to use it?
The API can be gotten from [JitPack](https://jitpack.io/#Strikeless/MCBootstrapHook), and here's a simple Spigot/Bukkit plugin example written in Java:
```Java
public class BootstrapHookPlugin extends JavaPlugin {
    
    private BootstrapHook bootstrapHook;

    @Override
    public void onEnable() {
        // BootstrapHook uses a builder design to create instances.
        bootstrapHook = BootstrapHook.builder()
                .channelInitializerName("ExampleInitializer")
                .channelInitializer(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // This will be called when a client has connected, after the server's own ChannelInitializer.
                        // Here we are just logging the pipeline's current handlers.
                        getLogger().info("Pipeline: " + String.join(", ", ch.pipeline().names()));

                        // We can also modify the pipeline however we want.
                        ch.pipeline().addBefore("packet_handler", "my_packet_handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // Do something with the received packet that has already been decoded by
                                // the server's own decoder, since we are injecting after it.
                            }
                        });
                    }
                })
                .build();

        // We also need to inject in order to have any effect!
        try {
            bootstrapHook.inject();
        } catch (final BootstrapHookException ex) {
            // We may fail to inject if for example the server is running under a strict security manager,
            // or if the server is running an exotic server JAR that comes with incompatibilities.
            getLogger().severe("Failed to inject!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // The ejection process should never fail, thus we don't need to catch any exceptions here.
        // Keep in mind that ejection will not cancel any modifications you've done in your channel initializers!
        bootstrapHook.eject();
    }
}
```
It may look daunting at first, but in the end it's rather simple and can be split into a few classes if needed to.

## License
MCBootstrapHook is currently licensed under GPLv3 due to it depending on Bukkit's API. If the API stops depending on Bukkit's API in the future, the license may be changed to one that's even more free, as long as it is compliant with Netty's license.

![GPLv3 logo](https://www.gnu.org/graphics/gplv3-127x51.png)
