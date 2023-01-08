Instructions:
    - Move DatapackLoader.jar to the server's 'plugins' folder, then start the server.
    - There are four options for adding datapacks:
        - Dragging and dropping by hand into the plugin's 'Datapacks' folder.
        - Pasting a URL into the '/dl import <url>' console command.
        - Pasting URLs into 'config.yml'.
        - Enabling 'starter-datapack' in 'config.yml'.
    - Configure 'config.yml' in the newly-generated 'DatapackLoader' folder to your preferences.
    - Restart the server with '/stop', then start it.

    URL file's type should be '.zip'.

Command | Permission | Description:
    - /dl help | datapackloader.help | Describes how to use the plugin.
    - /dl import <url> | (console only) | Imports a datapack from a .zip URL.
    - /dltp <worldname> | datapackloader.tp | Teleport to the given world's spawn with spectator mode.


If you are not using a control panel to manage the server, and want the server to start up after it shuts down, copy one of the following:
Note: "nogui" is not supported on versions of Minecraft 1.15 and below.

Windows: "start.bat"
    @ECHO OFF
    :start
    java -Xmx2G -Xms1G -jar MYSERVERJARNAME.jar -o false --nogui
    goto start

Mac: "start.command"
    #!/bin/bash
    cd "$(dirname "$0")"
    while true; do
    java -Xmx2G -Xms1G -jar MYSERVERJARNAME.jar nogui
    purge
    done

Linux, using screens: "start.sh"
    #!/bin/sh
    screen -dmS server java -Xmx2G -Xms1G -jar MYSERVERJARNAME.jar nogui
Terminal, in directory: chmod +x start.sh (or create another script that launches this script)
Run: /start.sh
Connect to screen: "screen -r"
Leave screen: Ctrl+A+D


Thank you for trying DatapackLoader!

DatapackLoader uses the "do whatever you want, please credit me" license.
DatapackLoader by lichenaut