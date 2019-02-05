package com.scs.stevetech1.systems.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.systems.AbstractSystem;

import ssmith.util.RealtimeInterval;

public class SoundSystem extends AbstractSystem {

	private RealtimeInterval removeNodesInterval = new RealtimeInterval(5000);
	private AssetManager assetManager;
	private Node gameNode;
	private List<ListData> nodesToRemove = new LinkedList<ListData>();

	public SoundSystem(AssetManager _assetManager, Node _node) {
		super();

		assetManager = _assetManager;
		gameNode = _node;
	}


	public void process() {
		if (removeNodesInterval.hitInterval()) {
			removeNodes();
		}
	}


	private void removeNodes() {
		Iterator<ListData> it = this.nodesToRemove.iterator();
		while (it.hasNext()) {
			ListData l = it.next();
			if (l.timeToRemove < System.currentTimeMillis()) {
				l.node.removeFromParent();
				it.remove();
			}
		}
	}


	public void playSound(String sound, int entityId, Vector3f pos, float volume, boolean stream) {
		//if (!Globals.MUTE) {
		if (sound != null && sound.length() > 0) {
			try {
				AudioNode node = new AudioNode(this.assetManager, sound, stream ? AudioData.DataType.Stream : AudioData.DataType.Buffer);
				if (pos != null) {
					node.setPositional(true);
					node.setLocalTranslation(pos);
				} else {
					node.setPositional(false);
				}
				node.setVolume(volume);
				node.setLooping(false);

				gameNode.attachChild(node);

				node.play();

				ListData data = new ListData();
				data.node = node;
				data.timeToRemove = System.currentTimeMillis() + (long)node.getPlaybackTime() + 1000;
				this.nodesToRemove.add(data);

			} catch (AssetLoadException ex) {
				Globals.pe("Error playing " + sound + ": " + ex.getMessage());
			} catch (AssetNotFoundException ex) {
				Globals.pe("Error playing " + sound + ": " + ex.getMessage());
			} catch (IllegalStateException ex) {
				// No sound card
				Globals.pe("Error playing " + sound + ": " + ex.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//}
	}


	class ListData {

		public Node node;
		public long timeToRemove;
	}

}
