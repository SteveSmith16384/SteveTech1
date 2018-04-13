package twoweeks;

import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class TwoWeeksCollisionValidator extends AbstractCollisionValidator {

	public boolean canCollide(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		return super.canCollide(a, b);
	}
}
