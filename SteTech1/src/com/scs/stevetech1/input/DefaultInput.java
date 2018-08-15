package com.scs.stevetech1.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public abstract class DefaultInput implements ActionListener {

    public static final String INPUT_MAPPING_EXIT = "SIMPLEAPP_Exit";
    
    protected boolean isExitPressed = false;

	public DefaultInput(InputManager inputManager) {
		inputManager.clearMappings();
		inputManager.clearRawInputListeners();
		
		inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addListener(this, INPUT_MAPPING_EXIT);

	}

	
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals(INPUT_MAPPING_EXIT)) {
			isExitPressed = true;
		}		
	}


	public boolean exitPressed() {
		return isExitPressed;
	}


}
