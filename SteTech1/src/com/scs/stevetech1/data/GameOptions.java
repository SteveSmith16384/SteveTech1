package com.scs.stevetech1.data;

public class GameOptions {

	public long deployDurationMillis = 10 * 1000;
	public long gameDurationMillis = 60 * 1000;
	public long finishedDurationMillis = 5 * 1000;
	public String ourExternalIP;
	public int ourExternalPort;
	public float avatarRestartTimeSecs, avatarInvulnDurationSecs;
	public int tickrateMillis, clientRenderDelayMillis, timeoutMillis, sendUpdateIntervalMillis;

	
	/**
	 * 
	 * @param _tickrateMillis
	 * @param _sendUpdateIntervalMillis
	 * @param _clientRenderDelayMillis
	 * @param _timeoutMillis
	 * @param _deployDuration
	 * @param _gameDuration
	 * @param _finishedDuration
	 * @param _ourExternalIP
	 * @param _ourExternalPort
	 * @param _avatarRestartTimeSecs
	 * @param _invulnDuration
	 */
	public GameOptions(int _tickrateMillis, int _sendUpdateIntervalMillis, int _clientRenderDelayMillis, int _timeoutMillis, 
			long _deployDuration, long _gameDuration, long _finishedDuration,
			String _ourExternalIP, int _ourExternalPort,
			float _avatarRestartTimeSecs, float _invulnDuration) {
		
		tickrateMillis = _tickrateMillis;
		sendUpdateIntervalMillis =_sendUpdateIntervalMillis;
		clientRenderDelayMillis = _clientRenderDelayMillis;
		timeoutMillis = _timeoutMillis;
		
		deployDurationMillis = _deployDuration;
		gameDurationMillis = _gameDuration;
		finishedDurationMillis = _finishedDuration;
		
		ourExternalIP = _ourExternalIP;
		ourExternalPort = _ourExternalPort;
		
		avatarRestartTimeSecs = _avatarRestartTimeSecs;
		avatarInvulnDurationSecs = _invulnDuration;
	}
	
}
