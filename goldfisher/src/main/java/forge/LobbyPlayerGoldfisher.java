package forge;

import forge.game.Game;
import forge.game.player.IGameEntitiesFactory;
import forge.game.player.Player;
import forge.game.player.PlayerController;

import java.util.Set;

public class LobbyPlayerGoldfisher extends LobbyPlayer implements IGameEntitiesFactory {

    private String GoldfishProfile = "";
    private boolean rotateProfileEachGame;
    private boolean allowCheatShuffle;
    private boolean useSimulation;

    public LobbyPlayerGoldfisher(String name, Set<GoldfisherAIOption> options) {
        super(name);
        if (options != null && options.contains(GoldfisherAIOption.USE_SIMULATION)) {
            this.useSimulation = true;
        }
    }

    public boolean isAllowCheatShuffle() {
        return allowCheatShuffle;
    }
    public void setAllowCheatShuffle(boolean allowCheatShuffle) {
        this.allowCheatShuffle = allowCheatShuffle;
    }

    public void setAiProfile(String profileName) {
        GoldfishProfile = profileName;
    }
    public String getAiProfile() {
        return GoldfishProfile;
    }

    public void setRotateProfileEachGame(boolean rotateProfileEachGame) {
        this.rotateProfileEachGame = rotateProfileEachGame;
    }

    private PlayerControllerGoldfisher createControllerFor(Player ai) {
        PlayerControllerGoldfisher result = new PlayerControllerGoldfisher(ai.getGame(), ai, this);
        result.setUseSimulation(useSimulation);
        result.allowCheatShuffle(allowCheatShuffle);
        return result;
    }

    @Override
    public PlayerController createMindSlaveController(Player master, Player slave) {
        return createControllerFor(slave);
    }

    @Override
    public Player createIngamePlayer(Game game, final int id) {
        Player ai = new Player(getName(), game, id);
        ai.setFirstController(createControllerFor(ai));

        if (rotateProfileEachGame) {
//            setAiProfile(AiProfileUtil.getRandomProfile());
            /*System.out.println(String.format("AI profile %s was chosen for the lobby player %s.", getAiProfile(), getName()));*/
        }
        return ai;
    }

    @Override
    public void hear(LobbyPlayer player, String message) { /* Local AI is deaf. */ }
}