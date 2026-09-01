package com.github.argon4w.acceleratedrendering.core.backends.states;

public class EmptyBindingState implements IBindingState {

	public static final IBindingState INSTANCE = new EmptyBindingState();

	@Override
	public void record() {

	}

	@Override
	public void restore() {

	}

	@Override
	public void delete() {

	}
}
