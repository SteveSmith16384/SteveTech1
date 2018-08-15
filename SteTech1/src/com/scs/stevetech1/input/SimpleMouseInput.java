package com.scs.stevetech1.input;

import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;

//todo - tidy this
public class SimpleMouseInput extends DefaultInput implements InputListener, IInputDevice, ActionListener {

	private boolean ability1 = false;

	public SimpleMouseInput(InputManager inputManager) {
		super(inputManager);
		
		inputManager.addMapping("Ability1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, "Ability1");
		inputManager.addMapping("Ability2", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, "Ability2");

	}


	@Override
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals("Ability1")) {
			ability1 = isPressed;
		} else {
			super.onAction(binding, isPressed, tpf);
		}
	}


	@Override
	public Vector3f getDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3f getLeft() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getFwdValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getBackValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getStrafeLeftValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getStrafeRightValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isJumpPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbilityPressed(int i) {
		return ability1;
	}


}
