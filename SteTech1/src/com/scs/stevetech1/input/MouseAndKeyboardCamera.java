package com.scs.stevetech1.input;

import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.scs.stevetech1.server.Globals;

public class MouseAndKeyboardCamera extends FlyByCamera implements ActionListener, IInputDevice { 

	//public static final String INPUT_MAPPING_EXIT = "SIMPLEAPP_Exit";

	private boolean left = false, right = false, up = false, down = false, jump = false, ability0 = false, ability1 = false, reload= false;
	private float mouseSens;

	public MouseAndKeyboardCamera(Camera cam, InputManager _inputManager, float _mouseSens) {
		super(cam);

		this.inputManager = _inputManager;
		mouseSens = _mouseSens;

		//inputManager.clearMappings();
		//inputManager.clearRawInputListeners();

		//inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		//inputManager.addListener(this, INPUT_MAPPING_EXIT);

		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addListener(this, "Left");
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addListener(this, "Right");
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addListener(this, "Up");
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addListener(this, "Down");
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "Jump");
		inputManager.addMapping("Ability1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, "Ability1");
		inputManager.addMapping("Ability2", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, "Ability2");
		inputManager.addMapping("Reload", new KeyTrigger(KeyInput.KEY_R));
		inputManager.addListener(this, "Reload");
		//inputManager.addMapping("CycleAbility", new KeyTrigger(KeyInput.KEY_C));
		//inputManager.addListener(this, "CycleAbility");

		// both mouse and button - rotation of cam
		inputManager.addMapping("mFLYCAM_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true), new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addListener(this, "mFLYCAM_Left");

		inputManager.addMapping("mFLYCAM_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false), new KeyTrigger(KeyInput.KEY_RIGHT));
		inputManager.addListener(this, "mFLYCAM_Right");

		inputManager.addMapping("mFLYCAM_Up", new MouseAxisTrigger(MouseInput.AXIS_Y, false), new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addListener(this, "mFLYCAM_Up");

		inputManager.addMapping("mFLYCAM_Down", new MouseAxisTrigger(MouseInput.AXIS_Y, true), new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addListener(this, "mFLYCAM_Down");

		// mouse only - zoom in/out with wheel, and rotate drag
		/*inputManager.addMapping("FLYCAM_ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addMapping("FLYCAM_ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping("FLYCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));*/

		// keyboard only WASD for movement and WZ for rise/lower height
		/*inputManager.addMapping("FLYCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("FLYCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("FLYCAM_Forward", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("FLYCAM_Backward", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("FLYCAM_Rise", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));*/

		//inputManager.addListener(this, mappings);  scs!
		inputManager.setCursorVisible(dragToRotate || !isEnabled());

		/*Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null && joysticks.length > 0){
            for (Joystick j : joysticks) {
                mapJoystick(j);
            }
        }*/
	}


	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (!enabled) {
			return;
		}

		value = value * mouseSens;

		if (name.equals("mFLYCAM_Left")){
			//Settings.p("name=" + name);
			rotateCamera(value, initialUpVec);
		}else if (name.equals("mFLYCAM_Right")){
			rotateCamera(-value, initialUpVec);
		}else if (name.equals("mFLYCAM_Up")){
			rotateCamera(-value * (invertY ? -1 : 1), cam.getLeft());
		}else if (name.equals("mFLYCAM_Down")){
			rotateCamera(value * (invertY ? -1 : 1), cam.getLeft());
		}/*else if (name.equals("FLYCAM_Forward")){
			moveCamera(value, false);
		}else if (name.equals("FLYCAM_Backward")){
			moveCamera(-value, false);
		}else if (name.equals("FLYCAM_StrafeLeft")){
			moveCamera(value, true);
		}else if (name.equals("FLYCAM_StrafeRight")){
			moveCamera(-value, true);
		}else if (name.equals("FLYCAM_Rise")){
			riseCamera(value);
		}else if (name.equals("FLYCAM_Lower")){
			riseCamera(-value);
		}else if (name.equals("FLYCAM_ZoomIn")){
			zoomCamera(value);
		}else if (name.equals("FLYCAM_ZoomOut")){
			zoomCamera(-value);
		}*/
	}


	@Override
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals("Left")) {
			left = isPressed;
		} else if (binding.equals("Right")) {
			right = isPressed;
		} else if (binding.equals("Up")) {
			up = isPressed;
		} else if (binding.equals("Down")) {
			down = isPressed;
		} else if (binding.equals("Jump")) {
			jump = isPressed;
		} else if (binding.equals("Ability1")) {
			ability0 = isPressed;
			/*if (Globals.DEBUG_CLICK_TO_SKIP) {
				Globals.p("ability0=" + ability0);
			}*/
		} else if (binding.equals("Ability2")) {
			ability1 = isPressed;
			/*if (Globals.DEBUG_CLICK_TO_SKIP) {
				Globals.p("ability1=" + ability1);
			}*/
			/*} else if (binding.equals(INPUT_MAPPING_EXIT)) {
			escape = isPressed;*/
		} else if (binding.equals("Reload")) {
			reload = isPressed;
			/*} else if (binding.equals(INPUT_MAPPING_EXIT)) {
			escape = isPressed;*/
		}		
	}


	@Override
	public boolean getFwdValue() {
		return up;
	}


	@Override
	public boolean getBackValue() {
		return down;
	}


	@Override
	public boolean getStrafeLeftValue() {
		return left;
	}


	@Override
	public boolean getStrafeRightValue() {
		return right;
	}        


	@Override
	public boolean isJumpPressed() {
		return jump;
	}


	@Override
	public boolean isAbilityPressed(int i) {
		switch (i) {
		case 0: return ability0;
		case 1: return ability1;
		default: throw new IllegalArgumentException("Invalid ability: " + i);
		}
	}        


	@Override
	public boolean isReloadPressed() {
		return reload;
	}


	@Override
	public Vector3f getDirection() {
		return cam.getDirection();
	}


	@Override
	public Vector3f getLeft() {
		return cam.getLeft();
	}


}
