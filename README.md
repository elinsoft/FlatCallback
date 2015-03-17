# FlatCallback
FlatCallback is java object that relief callback pain. It is designed for vert.x but you can easily modify it to adapt to another flatform.

what is callback pain?

Let's start with simple vert.x verticle.

You want to find someone from database and to change his cell phone number, and send the result back to event bus.
Code will be like below :

		mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
		new JsonObject()
		  .putString("action","select")
		  .putString("stmt","select id from customers where name=?")
		  .putArray( new JsonArray()
		    .addArray( new JsonArray()
		      .addString("tom")
		    )
		  )
		, 
		// heres callback
		new Handler<Message>() {
			@Override
			public void handle(Message find) {
				JsonObject jFind = (JsonObject)find.body();
				String status = returned.getString("status");
				if("ok".equals(status)) {
				  JsonArray result = jFind.getArray("result");
				  if(result.length()>0) {
  					int id = result.get(0).getObject("id");
  					
        		mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
        		new JsonObject()
        		  .putString("action","update")
        		  .putString("stmt","update customers set phone_number=? where id=?")
        		  .putArray( new JsonArray()
        		    .addArray( new JsonArray()
        		      .addString(newPhoneNumber)
        		      .addNumber(id)
        		    )
        		  )
          	,
          	// callback again
        		new Handler<Message>() {
        			@Override
        			public void handle(Message update) {
        				JsonObject jUpdate = (JsonObject)update.body();
        				if("ok".equals(jUpdate.getString("status")) {
        				  /* sucessful*/
        				}
        				else {
        				}
  					  }
  					 }
          }
          else {
            /* not found */
          }
				}
				else {
					/* handle error */
				}
			}
		});


		
Yes, it is simple logic but callback code is surely pain. Think abount 10 times of callback handling. It is callback pain.
So, what to do?

First, use java 8. Use lambda expression. It will relieve you from a lot of mess.
Above code can be simplified using java 8 :


		mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
		new JsonObject()
		  .putString("action","select")
		  .putString("stmt","select id from customers where name=?")
		  .putArray( new JsonArray()
		    .addArray( new JsonArray()
		      .addString("tom")
		    )
		  )
		, 
		// heres callback
		// changed to lambda expresssion
    		(Message<Object> find) -> {
      		JsonObject jFind = (JsonObject)find.body();
		String status = returned.getString("status");
			if("ok".equals(status)) {
			  JsonArray result = jFind.getArray("result");
			  if(result.length()>0) {
  				int id = result.get(0).getObject("id");
  				
      		mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
      		new JsonObject()
      		  .putString("action","update")
      		  .putString("stmt","update customers set phone_number=? where id=?")
      		  .putArray( new JsonArray()
      		    .addArray( new JsonArray()
      		      .addString(newPhoneNumber)
      		      .addNumber(id)
      		    )
      		  )
        	,
        	// callback again
      		// changed to lambda expresssion agin
    			(Message<Object> update) -> {
    				JsonObject jUpdate = (JsonObject)update.body();
    				if("ok".equals(jUpdate.getString("status")) {
    				  /* sucessful*/
    				}
    				else {
    				}
    			}
        }
        else {
          /* not found */
        }
			}
			else {
				/* handle error */
			
			}
		});


Flat callback make things easier.



  FlatCallback fc = new FlatCallback();
  fc.put("phone number",phoneNumber);
  
  fc.init(()->{
    mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
  		new JsonObject()
  		  .putString("action","select")
  		  .putString("stmt","select id from customers where name=?")
  		  .putArray( new JsonArray()
  		    .addArray( new JsonArray()
  		      .addString("tom")
  		    )
  		  )
  		, fc);
    }
  )
  .add((Message<Object> find) -> {
    mVertx.eventBus().send(Properties.JDBC_MODULE_ADDRESS,
    new JsonObject()
      .putString("action","update")
      .putString("stmt","update customers set phone_number=? where id=?")
      .putArray( new JsonArray()
        .addArray( new JsonArray()
          .addString(fc.get("phone nubmer"))
          .addNumber(id)
        )
      ) 
      ,fc}
    );
  })
  .finish(()->{
    /* handle finish */
  })
  .error((Exeption ex)->{
    /* handle error */
  });
  
  
Note that, exception is fired in each callback , FlatCallback can handle it. And FlatCallback supports map, so callbacks can put/get parameters to/from FlatCallback.

