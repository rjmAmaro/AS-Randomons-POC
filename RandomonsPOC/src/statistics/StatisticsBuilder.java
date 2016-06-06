package statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class StatisticsBuilder {
	private final static String QUEUE_NAME = "games_to_statistics";

	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("172.17.0.2");
		factory.setPort(5672);
		factory.setUsername("admin");
		factory.setPassword("rabbito_coco");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println(" [x] Received '" + message + "'");

				work(Long.parseLong(message));

			}
		};
		channel.basicConsume(QUEUE_NAME, true, consumer);
	}

	private static void work(long gameId) throws IOException {
		String gameHistoryFileName = new String("games/game" + gameId + ".txt");

		File file = new File(gameHistoryFileName);
		BufferedReader reader = null;

		reader = new BufferedReader(new FileReader(file));
		String text = null;
		String player1Id = null;
		String player2Id = null;
		
		int player1PointsTaked = 0;
		int player2PointsTaked = 0;

		while ((text = reader.readLine()) != null) {
			if(text.contains("GAME_START")) {
				player1Id = text.split("#")[2].split(":")[1];
				player2Id = text.split("#")[5].split(":")[1];
			}
			else if(text.contains("MOVE_ID")) {
				if(text.split("#")[2].split(":")[1].equals(player1Id))
					player1PointsTaked += Integer.parseInt(text.split("#")[3].split(":")[1]);
				else
					player2PointsTaked += Integer.parseInt(text.split("#")[3].split(":")[1]);
			}
		}
		
		System.out.println("Player " + player1Id + " # Pontos retirados: " + player1PointsTaked);
		System.out.println("Player " + player2Id + " # Pontos retirados: " + player2PointsTaked);
	}
}
