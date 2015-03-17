/******************************************
*
* flatten your callback
*
******************************************/
package com.elinsoft.securecollabo.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

public class FlatCallback implements Handler<Message<Object>> {

	Runnable mStart;
	Runnable mFinish;
	ErrorHandler mError;
	
	List<Handler<Message<Object>>> mHandlers;
	int mIndex = 0;
	HashMap<String, Object> mParams;
	public FlatCallback() {
		mHandlers = new ArrayList<Handler<Message<Object>>>();
		mParams = new HashMap<String, Object>();
	}

	public FlatCallback init(Runnable runnable) {
		mStart = runnable;
		return this;
	}
	public FlatCallback add(Handler<Message<Object>> handler) {
		mHandlers.add(handler);
		return this;
	}
	public FlatCallback finish(Runnable runnable) {
		mFinish = runnable;
		return this;
	}
	public void finish() {
		if(mFinish!=null) mFinish.run();
		mFinish = null;
	}
	public FlatCallback error(ErrorHandler errorHandler) {
		mError = errorHandler;
		return this;
	}
	
	@Override
	public void handle(Message<Object> message) {
		Handler<Message<Object>> handler = 
			(mIndex < mHandlers.size())? mHandlers.get(mIndex++) : null;
		if(handler!=null) {
			try {
				handler.handle(message);		
				if(mIndex >= mHandlers.size()) {
					if(mFinish!=null) mFinish.run();
				}
			}
			catch(Exception ex) {
				if(mError!=null) mError.handle(ex);
			}
		}
	}
	
	public void put(String key, Object val) {
		mParams.put(key, val);
	}
	
	public Object get(String key) {
		return mParams.get(key);
	}
	
	public void start() {
		try {
			mStart.run();
			if(mIndex >= mHandlers.size()) {
				if(mFinish!=null) mFinish.run();
			}
		}
		catch(Exception e) {
			if(mError!=null) {
			  mError.handle(ex);
			}
		}
	}
}

