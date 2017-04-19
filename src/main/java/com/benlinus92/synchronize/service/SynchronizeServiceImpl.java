package com.benlinus92.synchronize.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.benlinus92.synchronize.dao.SynchronizeDao;
import com.benlinus92.synchronize.model.Playlist;
import com.benlinus92.synchronize.model.Profile;
import com.benlinus92.synchronize.model.Room;
import com.benlinus92.synchronize.model.WaitingUser;

@Service("synchronizedService")
@Transactional
public class SynchronizeServiceImpl implements SynchronizeService {
	@Autowired
	SynchronizeDao dao;
	
	private ConcurrentHashMap<Integer, Integer> roomClientsMap;
	@Override
	public boolean saveUser(Profile user) {
		if(dao.isUserUnique(user)) {
			String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			user.setPassword(hashedPassword);
			dao.saveUser(user);
		} else 
			return false;
		return true;
	}

	@Override
	public Profile findUserByLogin(String login, boolean lazyInitialize) {
		return dao.findUserByLogin(login, lazyInitialize);
	}
	@Override
	public void editUserProfile(Profile editedUser, String login) {
		Profile oldUser = dao.findUserByLogin(login, false);
		if(oldUser != null) {
			oldUser.setPassword(BCrypt.hashpw(editedUser.getPassword(), BCrypt.gensalt()));
			oldUser.setEmail(editedUser.getEmail());
		}
	}
	@Override
	public void saveRoom(Room room, String userName) {
		Profile user = dao.findUserByLogin(userName, false);
		room.setUserId(user);
		dao.saveRoom(room);
	}

	@Override
	public List<Room> getAllRooms() {
		return dao.getAllRooms();
	}
	@Override
	public Room findRoomById(int id) {
		return dao.findRoomById(id);
	}
	@Override
	public boolean deleteRoomById(int id, String userName) {
		Room room = dao.findRoomById(id);
		if(room.getUserId().getLogin().equals(userName)) {
			List<Playlist> videos = room.getVideosList();
			for(Playlist video: videos)
				dao.deleteVideoById(video.getVideoId());
			dao.deleteRoomById(id);
			return true;
		}
		return false;
	}
	@Override
	public void saveVideo(Playlist video, int roomId) {
		Room room = dao.findRoomById(roomId);
		video.setRoom(room);
		dao.saveVideo(video);
	}
	@Override
	public List<Playlist> getAllVideos() {
		return dao.getAllVideos();
	}
	@Override
	public Playlist findVideoById(String videoId) {
		return dao.findVideoById(Integer.parseInt(videoId));
	}
	@Override
	public void deleteVideo(int videoId) {
		dao.deleteVideoById(videoId);
	}
	@Override
	public void updateVideo(int videoId, String currTime) {
		dao.updateVideoTime(videoId, currTime);
	}

	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public List<Playlist> getVideoListFromRoom(int roomId) {
		List<Playlist> list = dao.findRoomById(roomId).getVideosList();;
		return list;
	}

	@Override
	public List<WaitingUser> findWaitingUsersByRoom(int roomId) {
		dao.findWaitingUsersByRoom(roomId);
		return null;
	}

	@Override
	public void createAndSaveWaitingUser(String sessionId, String login, String roomId, String videoId) { 
		WaitingUser user = new WaitingUser(sessionId, login, dao.findRoomById(Integer.parseInt(roomId)), dao.findVideoById(Integer.parseInt(videoId)));
		dao.saveWaitingUser(user);
	}

	@Override
	public void deleteWaitingUser(String sessionId) {
		dao.deleteWaitingUserBySession(sessionId);
	}
	public void increaseRoomUsersCounter(int roomId) {
		if(roomClientsMap.containsKey(roomId)) {
			roomClientsMap.put(roomId, roomClientsMap.get(roomId) + 1);
		} else
			roomClientsMap.put(roomId, 0);
	}
	public void decreaseRoomUsersCounter(int roomId) {
		if(roomClientsMap.containsKey(roomId)) {
			int clients = roomClientsMap.get(roomId);
			if(clients - 1 <= 0)
				roomClientsMap.remove(roomId);
			else
				roomClientsMap.put(roomId, clients - 1);
		}
	}
}
