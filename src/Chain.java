public class Chain<Param, Result> {
	private Param param;
	private Result result;
	private ChainBlock block;
	private HandleBlock handleBlock;
	private Chain next;
	private boolean ready, runInUI;
	
	//region Start
	public static <R> Chain<Void, R> start(FirstBlock<R> firstBlock, HandleBlock handleBlock) {
		return runFirstNode(firstBlock, handleBlock, false);
	}
	
	public static <R> Chain<Void, R> start(FirstBlock<R> firstBlock) {
		return runFirstNode(firstBlock, null, false);
	}
	
	public static <R> Chain<Void, R> start(Block firstBlock, HandleBlock handleBlock) {
		return runFirstNode(() -> {
			firstBlock.run();
			return null;
		}, handleBlock, false);
	}
	
	public static <R> Chain<Void, R> start(Block firstBlock) {
		return runFirstNode(() -> {
			firstBlock.run();
			return null;
		}, null, false);
	}
	//endregion
	
	//region Start UI
	public static <R> Chain<Void, R> startUI(FirstBlock<R> firstBlock, HandleBlock handleBlock) {
		return runFirstNode(firstBlock, handleBlock, true);
	}
	
	public static <R> Chain<Void, R> startUI(FirstBlock<R> firstBlock) {
		return runFirstNode(firstBlock, null, true);
	}
	
	public static <R> Chain<Void, R> startUI(Block firstBlock, HandleBlock handleBlock) {
		return runFirstNode(() -> {
			firstBlock.run();
			return null;
		}, handleBlock, true);
	}
	
	public static <R> Chain<Void, R> startUI(Block firstBlock) {
		return runFirstNode(() -> {
			firstBlock.run();
			return null;
		}, null, true);
	}
	//endregion
	
	//region Then
	public <Result2> Chain<Result, Result2> then(ChainBlock<Result, Result2> block, HandleBlock handleBlock) {
		next = new Chain<Result, Result2>();
		next.block = block;
		next.handleBlock = handleBlock;
		
		if (ready) {
			next.param = result;
			next.execute();
		}
		
		return next;
	}
	
	public <Result2> Chain<Result, Result2> then(ChainBlock<Result, Result2> block) {
		return then(block, null);
	}
	
	public <Result2> Chain<Result, Result2> then(FirstBlock<Result2> block, HandleBlock handleBlock) {
		return then((ChainBlock<Result, Result2>) ignore -> block.execute(), handleBlock);
	}
	
	public <Result2> Chain<Result, Result2> then(FirstBlock<Result2> block) {
		return then((ChainBlock<Result, Result2>) ignore -> block.execute(), null);
	}
	
	public <Result2> Chain<Result, Result2> then(LastBlock<Result> block) {
		return then(p -> {
			block.execute(p);
			return null;
		}, null);
	}
	
	public <Result2> Chain<Result, Result2> then(LastBlock<Result> block, HandleBlock handleBlock) {
		return then(p -> {
			block.execute(p);
			return null;
		}, handleBlock);
	}
	
	public <Result2> Chain<Result, Result2> then(Block block) {
		return then(p -> {
			block.run();
			return null;
		}, null);
	}
	
	public <Result2> Chain<Result, Result2> then(Block block, HandleBlock handleBlock) {
		return then(p -> {
			block.run();
			return null;
		}, handleBlock);
	}
	//endregion
	
	//region Then UI
	public <Result2> Chain<Result, Result2> thenUI(ChainBlock<Result, Result2> block, HandleBlock handleBlock) {
		next = new Chain<Result, Result2>();
		next.block = block;
		next.handleBlock = handleBlock;
		next.runInUI = true;
		
		if (ready) {
			next.param = result;
			next.execute();
		}
		
		return next;
	}
	
	public <Result2> Chain<Result, Result2> thenUI(ChainBlock<Result, Result2> block) {
		return thenUI(block, null);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(FirstBlock<Result2> block, HandleBlock handleBlock) {
		return thenUI((ChainBlock<Result, Result2>) ignore -> block.execute(), handleBlock);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(FirstBlock<Result2> block) {
		return thenUI((ChainBlock<Result, Result2>) ignore -> block.execute(), null);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(LastBlock<Result> block, HandleBlock handleBlock) {
		return thenUI(p -> {
			block.execute(p);
			return null;
		}, handleBlock);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(LastBlock<Result> block) {
		return thenUI(p -> {
			block.execute(p);
			return null;
		}, null);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(Block block, HandleBlock handleBlock) {
		return thenUI(p -> {
			block.run();
			return null;
		}, handleBlock);
	}
	
	public <Result2> Chain<Result, Result2> thenUI(Block block) {
		return thenUI(p -> {
			block.run();
			return null;
		}, null);
	}
	//endregion
	
	//region End
	public void end(LastBlock<Result> block, HandleBlock handleBlock) {
		then(p -> {
			block.execute(p);
			return null;
		}, handleBlock);
	}
	
	public void end(LastBlock<Result> block) {
		end(block, null);
	}
	
	public void end(Block block, HandleBlock handleBlock) {
		end(ignore -> block.run(), handleBlock);
	}
	
	public void end(Block block) {
		end(ignore -> block.run(), null);
	}
	//endregion
	
	//region End UI
	public void endUI(LastBlock<Result> block, HandleBlock handleBlock) {
		thenUI(p -> {
			block.execute(p);
			return null;
		}, handleBlock);
	}
	
	public void endUI(LastBlock<Result> block) {
		endUI(block, null);
	}
	
	public void endUI(Block block, HandleBlock handleBlock) {
		endUI(ignore -> block.run(), handleBlock);
	}
	
	public void endUI(Block block) {
		endUI(ignore -> block.run(), null);
	}
	//endregion
	
	private void execute() {
		Runnable target = () -> {
			try {
				result = (Result) block.execute(param);
			} catch (Exception e) {
				if (handleBlock != null) handleBlock.handle(e);
				else throw new RuntimeException(e);
			}
			
			if (next != null) {
				next.param = this.result;
				next.execute();
			} else ready = true;
		};
		
		if (runInUI) {
			if (isMainThread()) target.run();
			else ThreadPool.post(target);
		} else {
			if (isMainThread()) ThreadPool.main.execute(target);
			else target.run();
		}
	}
	
	private static <P, R> Chain<P, R> runFirstNode(FirstBlock<R> firstBlock, HandleBlock handleBlock, boolean runInUI) {
		Chain<P, R> node = new Chain<>();
		node.block = ignore -> firstBlock.execute();
		node.handleBlock = handleBlock;
		node.runInUI = runInUI;
		node.execute();
		
		return node;
	}
	
	private static boolean isMainThread() {
		return Looper.getMainLooper() == Looper.myLooper();
	}
	
	//region Blocks
	public interface Block {
		void run() throws Exception;
	}
	
	public interface FirstBlock<Result> {
		Result execute() throws Exception;
		
	}
	
	public interface ChainBlock<Param, Result> {
		Result execute(Param p) throws Exception;
		
	}
	
	public interface LastBlock<Param> {
		void execute(Param p) throws Exception;
		
	}
	
	public interface HandleBlock {
		void handle(Exception e);
	}
	//endregion
}
