package edu.lewisu.cs.hdondiego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

abstract class CameraEffect {
    protected OrthographicCamera cam;
    protected int duration, progress;
    protected ShapeRenderer renderer;
    protected SpriteBatch batch;
    public CameraEffect(OrthographicCamera cam, int duration, 
    SpriteBatch batch, ShapeRenderer renderer) {
        this.cam = cam;
        this.duration = duration;
        this.batch = batch;
        this.renderer = renderer;
        progress = duration;
    }
    public boolean isActive() {
        return (progress<duration);
    }
    public abstract void play();
    public void updateCamera() {
        cam.update();
        if (renderer != null) {
            renderer.setProjectionMatrix(cam.combined);
        }
        if (batch != null) {
            batch.setProjectionMatrix(cam.combined);
        }
    }
    public void start() {
        progress = 0;
    }
}

class CameraShake extends CameraEffect {
    private int intensity;
    private int speed;
    public int getIntensity() {
        return intensity;
    }
    public void setIntensity(int intensity) {
        if (intensity < 0) {
            this.intensity = 0;
        } else {
            this.intensity = intensity;
        }
    }
    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = 0;
        } else {
            if (speed > duration) {
                speed = duration / 2;
            } else {
                this.speed = speed;
            }
        }
    }
    @Override
    public boolean isActive() {
        return super.isActive() && speed > 0;
    }
    public CameraShake(OrthographicCamera cam, int duration, SpriteBatch batch,
    ShapeRenderer renderer, int intensity, int speed) {
        super(cam,duration,batch,renderer);
        setIntensity(intensity);
        setSpeed(speed);
    }
    
    // progress is initially 0
    // dampenFactor initially starts off as 1
    // as the value of progress increases, the dampenFactor gets smaller
    // this function is used to help change the completeness of the shaking
    // starts shaking at full capacity at the start, and slowly shakes less
    public float dampen() {
    	float dampenFactor = 1 - ((float)progress / duration);
    	return dampenFactor;
    }
    
    @Override
    public void play() {
        if (isActive()) {
            if (progress % speed == 0) {
                intensity = -intensity;
                cam.translate(0,2*intensity*dampen());	// altered to move up in the y-direction
            }
            progress++;
            if (!isActive()) {
                cam.translate(0,-intensity * (dampen()));	// altered to move down in the y-direction
            }
            updateCamera();
        }
    }
    @Override
    public void start() {
        super.start();
        cam.translate(0, intensity);	// the change in intensity in the y-direction
        updateCamera();
    }
}


class InputHandler extends InputAdapter {
	private boolean shiftHeld = false;
	private SpriteBatch batch;
	private OrthographicCamera cam;
	private Vector3 startCam, startMouse;
	private String keyIntensity = "0";
	
	public InputHandler(SpriteBatch batch, OrthographicCamera cam) {
		this.batch = batch;
		this.cam = cam;
	}
	
	// responsible for appending each number entered
	// helps to change the percentage of speed
	// will use Integer.parseInt(keyIntensity)
	// to actually get the intensity integer entered
	public void appendKeyIntensity(int keyValue) {
		this.keyIntensity += keyValue;
	}
	
	public int getKeyIntensity() {
		return Integer.parseInt(keyIntensity);
	}
	
	public void setKeyIntensity(String value) {
		keyIntensity = value;
	}
	
	@Override
	public boolean keyDown(int keyCode) {
		// plays an important role to help change the context of panning the camera to move around
		// to rotating the camera by the angle the mouse is moved
		if (keyCode == Keys.SHIFT_LEFT || keyCode == Keys.SHIFT_RIGHT) {
			shiftHeld = true;
		}
		if (keyCode == Keys.SPACE) {
			if (shiftHeld) {
				System.out.println("Shiftfire!");
			} else {
				System.out.println("Fire!");
			}
		}
		
		// pressing each number key helps append a number to a String
		// which is later used to set the user-entered intensity for the camera shake
		switch(keyCode) {
			case Keys.NUM_0:
				appendKeyIntensity(0);
				break;
			case Keys.NUM_1:
				appendKeyIntensity(1);
				break;
			case Keys.NUM_2:
				appendKeyIntensity(2);
				break;
			case Keys.NUM_3:
				appendKeyIntensity(3);
				break;
			case Keys.NUM_4:
				appendKeyIntensity(4);
				break;
			case Keys.NUM_5:
				appendKeyIntensity(5);
				break;
			case Keys.NUM_6:
				appendKeyIntensity(6);
				break;
			case Keys.NUM_7:
				appendKeyIntensity(7);
				break;
			case Keys.NUM_8:
				appendKeyIntensity(8);
				break;
			case Keys.NUM_9:
				appendKeyIntensity(9);
				break;
		}
		
		
		System.out.printf("keyIntensity: %s\n", keyIntensity);
		return true; // this function completely handles the event
					// if it's false, it is meaningless because there's nothing else to pick up the input
					// if it is set to false, "Fire" will be printed twice using plexi
	}
	
	@Override
	public boolean keyUp(int keyCode) {
		if (keyCode == Keys.SHIFT_LEFT || keyCode == Keys.SHIFT_RIGHT) {
			shiftHeld = false;
		}
		return true;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		startCam = new Vector3(cam.position.x, cam.position.y, 0);
		startMouse = new Vector3(screenX, screenY, 0);
		return true;
	}
	
	public void updateCamera() {
		cam.update();
		batch.setProjectionMatrix(cam.combined);
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		boolean offScreenX = false;
		boolean offScreenY = false;
		
		// checks to make sure that the location where the mouse is clicked and dragged
		// is within the x-axis of the window of the game
		if (screenX < 0 || screenX >= Gdx.graphics.getWidth()) {
			offScreenX = true;
		}
		
		// checks to make sure that the location where the mouse is clicked and dragged
		// is within the y-axis of the window of the game
		if (screenY < 0 || screenY >= Gdx.graphics.getHeight()) {
			offScreenY = true;
		}
		
		// as long as the coordinates of the dragging mouse is within the window
		// of the game
		if (!offScreenX && !offScreenY) {
			// proceed to do the diffX and diffY calculations
			float diffX = screenX - startMouse.x;
			float diffY = screenY - startMouse.y;
			
			// if the shift key was pressed and holded
			if (shiftHeld) {
				// go ahead and change the camera rotation based on the direction
				// the camera was dragged
				double theta = Math.atan2(diffY, diffX);
				cam.rotate((float)theta);
			} else {
				// otherwise, just pan the camera
				cam.position.x = startCam.x + diffX;
				cam.position.y = startCam.y - diffY;
			}
			
			updateCamera(); // game programmers may frown upon this because this is called to the stack
		}
		
		return true;
	}
}

public class DiscreteEH_and_CameraEffects extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	OrthographicCamera cam;
	int WIDTH;
	int HEIGHT;
	CameraShake shaker;
	InputHandler handler1;
	int imgX, imgY; // state variables associated with the location
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		imgX = 0;
		imgY = 0;
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		cam = new OrthographicCamera(WIDTH,HEIGHT);
		handler1 = new InputHandler(batch, cam);
		Gdx.input.setInputProcessor(handler1);
		cam.translate(WIDTH/2, HEIGHT/2);
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		shaker = new CameraShake(cam, 100, batch, null, 10, 2);
	}

	public void handleInput() {
		boolean cameraNeedsUpdating = false;
		
		// moves camera in upward direction
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			cam.translate(0,1);
			cameraNeedsUpdating = true;
		}
		
		// moves camera in downward direction
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			cam.translate(0,-1);
			cameraNeedsUpdating = true;
		}
		
		// moves camera in left direction
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			cam.translate(-1,0);
			cameraNeedsUpdating = true;
		}
		
		// moves camera in right direction
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			cam.translate(1,0);
			cameraNeedsUpdating = true;
		}
		
		// exits/closes the game
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		
		// changes parameter of y-coordinate location to draw image in positive (up) direction
		if (Gdx.input.isKeyJustPressed(Keys.W)) {
			imgY += 5;
		}
		
		// changes parameter of y-coordinate location to draw image in negative (down) direction
		if (Gdx.input.isKeyJustPressed(Keys.S)) {
			imgY -= 5;
		}
		
		// changes parameter of x-coordinate location to draw image in positive (right) direction
		if (Gdx.input.isKeyJustPressed(Keys.A)) {
			imgX -=5;
		}
		
		// changes parameter of x-coordinate location to draw image in negative (left) direction
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			imgX +=5;
		}
		
		// update the camera if the camera is translated
		if (cameraNeedsUpdating) {
			updateCamera();
		}
	}
	
	public void updateCamera() {
		cam.update();
		batch.setProjectionMatrix(cam.combined);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput();
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			int newIntensity = handler1.getKeyIntensity();
			
			if (newIntensity > 100) {
				newIntensity = 100;
			} else if (newIntensity <= 0) {
				newIntensity = 0;
			}
			
			shaker.setIntensity(newIntensity);
			shaker.start();
			handler1.setKeyIntensity("0");
		}
		shaker.play();
		batch.begin();
		batch.draw(img, imgX, imgY);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
