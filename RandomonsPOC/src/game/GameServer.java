package game;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import bo.Randomon;

public class GameServer {
	private final static String QUEUE_NAME = "games_to_statistics";
	private static long gameId = 1;

	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("rabbitmq");
		factory.setPort(5672);
		factory.setUsername("admin");
		factory.setPassword("rabbito_coco");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);


		String gameHistoryFileName = new String("games/game" + (gameId) + ".txt"); 

		String player1Id = "P1111";
		String player1RandomonId = "P1111:R1001";
		int player1RandomonLifePoints = 100;
		int player1RandomonAttackValue = 12;
		Randomon r1 = new Randomon(player1Id, player1RandomonId, player1RandomonLifePoints, player1RandomonAttackValue);

		String player2Id = "P2222";
		String player2RandomonId = "P2222:R1004";
		int player2RandomonLifePoints = 100;
		int player2RandomonAttackValue = 5;
		Randomon r2 = new Randomon(player2Id, player2RandomonId, player2RandomonLifePoints, player2RandomonAttackValue);

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(gameHistoryFileName), "utf-8"))) {
			writer.write(LocalDateTime.now().toString()
					+ "#GAME_START"
					+ "#INVITER_PLAYER_ID:" + player1Id
					+ "#INVITER_PLAYER_RANDOMON_ID:" + player1RandomonId
					+ "#INVITER_PLAYER_RANDOMON_STARTING_LIFE_POINTS:" + player1RandomonLifePoints
					+ "#INVITED_PLAYER_ID:" + player2Id
					+ "#INVITED_PLAYER_RANDOMON_ID:" + player2RandomonId
					+ "#INVITED_PLAYER_RANDOMON_STARTING_LIFE_POINTS:" + player2RandomonLifePoints + "\n");

			int move = 1;

			while(r1.lifePoints > 0 && r2.lifePoints > 0) {
				if(move % 2 == 1) {
					makeAttack(writer, move, r1, r2);
				}
				else {
					makeAttack(writer, move, r2, r1);
				}

				move++;
			}

			Randomon winner = null, loser = null;

			if(r1.lifePoints > 0) {
				winner = r1;
				loser = r2;
			}
			else {
				winner = r2;
				loser = r1;
			}

			writer.write(LocalDateTime.now().toString()
					+ "#GAME_FINNISH"
					+ "#WINNER_PLAYER_ID:" + winner.ownerId
					+ "#WINNER_PLAYER_RANDOMON_ID:" + winner.id
					+ "#WINNER_PLAYER_RANDOMON_FINAL_LIFE_POINTS:" + winner.lifePoints
					+ "#LOSER_PLAYER_ID:" + loser.ownerId
					+ "#LOSER_PLAYER_RANDOMON_ID:" + loser.id
					+ "#LOSER_PLAYER_RANDOMON_FINAL_LIFE_POINTS:" + loser.lifePoints + "\n");

			writer.close();

			String message = Long.toString(gameId++);

			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
			channel.close();
			connection.close();
		}
	}

	private static void makeAttack(Writer writer, int moveId, Randomon attacker, Randomon receiver) throws IOException {
		System.out.println("A: " + attacker.id + " # R: " + receiver.id + " # pp:" + attacker.attackValue);
		receiver.sufferAttack(attacker.attackValue);
		registerAttack(writer, moveId, attacker, receiver);
		System.out.println("RESULT - A: " + attacker.id + " - pp: " + attacker.lifePoints + " # R: " + receiver.id + " - pp: " + receiver.lifePoints);
	}

	private static void registerAttack(Writer writer, int moveId, Randomon attacker, Randomon receiver) throws IOException {
		writer.write(LocalDateTime.now().toString()
				+ "#MOVE_ID:" + moveId 
				+ "#ATTACKER_ID:" + attacker.id 
				+ "#RECEIVER_ID:" + receiver.id
				+ "#RECEIVER_RESULT_LIFE_VALUE:" + receiver.lifePoints + "\n");
	}
}
