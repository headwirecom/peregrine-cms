package com.peregrine.commons.concurrent;

public interface Callback<Argument> {

	void call(Argument arg);

}
