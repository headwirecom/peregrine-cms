package com.peregrine.concurrent;

public interface Callback<Argument> {

	void call(Argument arg);

}
