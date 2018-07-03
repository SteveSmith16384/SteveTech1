package com.scs.stevetech1.hud;

import com.jme3.scene.Node;
import com.scs.stevetech1.client.AbstractGameClient;

public interface IHUD {

	Node getRootNode();

	void processByClient(AbstractGameClient client, float tpf_secs);

	void setDebugText(String s);

	void showDamageBox();

	void showMessage(String s);

	void addItem(Node n);

	//void setLog(LinkedList<String> gameLog);
	void appendToLog(String s);


}
