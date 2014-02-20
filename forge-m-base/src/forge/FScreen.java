package forge;

import com.badlogic.gdx.Screen;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinImage;
import forge.assets.FSkinColor.Colors;
import forge.toolbox.FDisplayObject;

public abstract class FScreen extends FDisplayObject implements Screen {
    private static final FSkinColor clrTheme = FSkinColor.get(Colors.CLR_THEME);

    @Override
    public final void resize(int width, int height) {
        setBounds(0, 0, width, height);
        doLayout(width, height);
    }

    @Override
    public final void render(float delta) {
        draw(new Graphics());
    }

    @Override
    public final void draw(Graphics g) {
        drawBackground(g);
        super.draw(g);
    }

    protected void drawBackground(Graphics g) {
        g.drawImage(FSkinImage.BG_TEXTURE, 0, 0, getWidth(), getHeight());
        g.fillRect(clrTheme, 0, 0, getWidth(), getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }
}
