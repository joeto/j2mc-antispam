package to.joe.j2mc.antispam;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class J2MC_Antispam extends JavaPlugin implements Listener {
    private class Check implements Runnable {
        private final String name;

        public Check(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            final PlayerChat chat = J2MC_Antispam.this.data.get(this.name);
            if (chat != null) {
                final int result = chat.check();
                if (result > J2MC_Antispam.this.threshold) {

                }
            }
        }
    }

    private class DelName implements Runnable {
        private final String name;

        public DelName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            J2MC_Antispam.this.getServer().getScheduler().cancelTask(J2MC_Antispam.this.data.remove(this.name).id());
        }
    }

    private class PlayerChat {
        private int warnings;
        private int messages;
        private final int id;

        public PlayerChat(int id) {
            this.id = id;
            this.warnings = 0;
            this.messages = 0;
        }

        public int check() {
            if (this.messages > J2MC_Antispam.this.persec) {
                this.warnings++;
            }
            this.messages = 0;
            return this.warnings;
        }

        public int id() {
            return this.id;
        }

        public void update() {
            this.messages++;
        }
    }

    private final int threshold;

    private final int persec;

    private HashMap<String, PlayerChat> data;

    public J2MC_Antispam() {
        this.threshold = this.getConfig().getInt("warningsthreshold", 3);
        this.persec = this.getConfig().getInt("messagepersecond", 3);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if (!event.isCancelled()) {
            this.update(event.getPlayer().getName());
        }
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onMe(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/me ")) {
            this.update(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new DelName(event.getPlayer().getName()));
    }

    private void update(String name) {
        PlayerChat chat = this.data.get(name);
        if (name == null) {
            chat = new PlayerChat(this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Check(name), 20));
            this.data.put(name, chat);
        }
        chat.update();
    }
}
