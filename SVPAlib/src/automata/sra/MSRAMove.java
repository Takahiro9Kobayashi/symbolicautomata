/**
 * SVPAlib
 * automata
 * Sep 21, 2018
 * @author Tiago Ferreira
 */
package automata.sra;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;

/**
 * MSRAMove
 * @param <P> set of predicates over the domain S
 * @param <S> domain of the automaton alphabet
 */
public class MSRAMove<P, S> extends SRAMove<P, S> {

	/**
	 * Constructs a multiple assignment SRA Transition that starts from state <code>from</code> and ends at state
	 * <code>to</code> with input <code>input</code>
     * MSRA transitions happen iff the input symbol is in all E, if this is the case the input is assigned to all U.
	 */
	public MSRAMove(Integer from, Integer to, P guard, Set<Integer> E, Set<Integer> U) {
		super(from, to, guard, E, Collections.emptySet(), U);
    }

	@Override
	public boolean isSatisfiable(BooleanAlgebra<P, S> boolal) throws TimeoutException {
        return boolal.IsSatisfiable(guard);
	}

    @Override
    //TODO
    public S getWitness(BooleanAlgebra<P, S> boolal, LinkedList<S> registerValues) throws TimeoutException {
        P predicates = boolal.True();
        // Must be "in" all E registers, i.e. must be equal to the values in all E registers.
        for (Integer ERegister : E)
        	if (registerValues.get(ERegister) != null)
            	predicates = boolal.MkAnd(predicates, boolal.MkAtom(registerValues.get(ERegister)));
        	else
				predicates = boolal.MkAnd(predicates, boolal.False());
        // Must not be "in" any U registers, i.e. must not be equal to any value in U registers.
        for (Integer URegister : U)
			if (registerValues.get(URegister) != null)
            	predicates = boolal.MkAnd(predicates, boolal.MkNot(boolal.MkAtom(registerValues.get(URegister))));

        return boolal.generateWitness(boolal.MkAnd(guard, predicates)); 
    }

    @Override
    public boolean hasModel(S input, BooleanAlgebra<P, S> boolal, LinkedList<S> registerValues) throws TimeoutException {
        P predicates = boolal.True();
        // Must be "in" all E registers, i.e. must be equal to the values in all E registers.
        if (E.isEmpty() && U.isEmpty())
            return false;

        LinkedList<Integer> registersWithInput = new LinkedList<>();
        for (int index = 0; index < registerValues.size(); index++)
            if(registerValues.get(index) != null && registerValues.get(index).equals(input))
                registersWithInput.add(index);
         return new HashSet<>(registersWithInput).equals(new HashSet<>(E)) && boolal.HasModel(guard, input);

    }

	@Override
	public boolean isDisjointFrom(SRAMove<P, S> t, BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (from.equals(t.from)) {
            if (E != t.E || U != t.U) {
                return true;
            }
			MSRAMove<P, S> MSRAt = (MSRAMove<P, S>) t;
			if(ba.IsSatisfiable(ba.MkAnd(guard, MSRAt.guard)) || (E == MSRAt.E && U == MSRAt.U))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("S: %s -%s/{%s},{%s}-> %s", from, guard, E, U, to);
	}

	@Override
	public String toDotString() {
		return String.format("%s -> %s [label=\"%s/{%s},{%s}\"]\n", from, to, guard, E, U);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MSRAMove<?, ?>) {
			MSRAMove<?, ?> otherCasted = (MSRAMove<?, ?>) other;
			return otherCasted.from.equals(from) && otherCasted.to.equals(to) &&
                   otherCasted.guard.equals(guard) &&
                   otherCasted.E.equals(E) &&
                   otherCasted.I.equals(I) &&
				   otherCasted.U.equals(U);
		}

		return false;
	}

	@Override
	public Object clone(){
		  return new MSRAMove<P, S>(from, to, guard, E, U);
	}

    @Override
    public LinkedList<MSRAMove<P, S>> asMultipleAssignment(LinkedList<S> registerValues) {
        return (new LinkedList<MSRAMove<P, S>>(Collections.singletonList(this)));
    }

    public boolean isFresh() {
        return false;
    }

	public boolean isStore() {
		return false;
	}

    public boolean isMultipleAssignment() {
        return true;
    }
}
