package bo;

public class Randomon {
	public String ownerId;
	public String id;
	public int lifePoints;
	public int attackValue;
	
	public Randomon(String ownerId, String id, int lifePoints, int attackValue) {
		this.ownerId = ownerId;
		this.id = id;
		this.lifePoints = lifePoints;
		this.attackValue = attackValue;
	}
	
	public void sufferAttack(int attackValue) {
		this.lifePoints -= attackValue;
		if(this.lifePoints < 0)
			this.lifePoints = 0;
	}
}
