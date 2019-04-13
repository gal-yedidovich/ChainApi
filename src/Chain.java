
public final class Chain {
	private BlockNode head;
	private LastBlock<Exception> handleBlock;

	private Chain() {
	}

	public static <R> BlockNode<Void, R> start(FirstBlock<R> firstBlock) {
		Chain c = new Chain();

		BlockNode<Void, R> node = new BlockNode<>();
		node.block = p -> firstBlock.execute();
		node.chain = c;

		c.head = node;
		return node;
	}

	public static <R> BlockNode<Void, R> startUI(FirstBlock<R> firstBlock) {
		Chain c = new Chain();

		BlockNode<Void, R> node = new BlockNode<>();
		node.block = p -> firstBlock.execute();
		node.chain = c;
		node.runInUI = true;

		c.head = node;
		return node;
	}

	public Chain handle(LastBlock<Exception> handleBlock) {
		this.handleBlock = handleBlock;
		return this;
	}

	public void execute() {
		head.execute();
	}

	private static boolean isMainThread() {
		return false;
	}

	private static void async(Runnable r) {
		r.run();
	}

	public static class BlockNode<Param, Result> {
		private Param param;
		private ChainBlock<Param, Result> block;
		private BlockNode next;
		private boolean runInUI;
		private Chain chain;

		private BlockNode() {
		}

		public <Result2> BlockNode<Result, Result2> then(ChainBlock<Result, Result2> block) {
			BlockNode<Result, Result2> next = new BlockNode<>();
			next.block = block;
			this.next = next;
			next.chain = chain;

			return next;
		}

		public <Result2> BlockNode<Result, Result2> thenUI(ChainBlock<Result, Result2> block) {
			BlockNode<Result, Result2> next = new BlockNode<>();
			next.block = block;
			next.runInUI = true;
			this.next = next;
			next.chain = chain;

			return next;
		}

		public Chain end(LastBlock<Result> block) {
			then(p -> {
				block.execute(p);
				return null;
			});

			return chain;
		}

		public Chain endUI(LastBlock<Result> block) {
			thenUI(p -> {
				block.execute(p);
				return null;
			});

			return chain;
		}

		private void execute() {
			Runnable target = () -> {
				try {
					Result result = block.execute(param);
					if (next != null) {
						next.param = result;
						next.execute();
					}
				} catch (Exception e) {
					if (chain.handleBlock != null) chain.handleBlock.execute(e);
					else throw e; //rethrow if no handling provided
				}
			};

			if (runInUI) {
				if (isMainThread()) target.run();
				else target.run();
			} else {
				if (isMainThread()) async(target);
				else target.run();
			}
		}
	}

	//region Blocks
	public interface FirstBlock<Result> {
		Result execute();
	}

	public interface ChainBlock<Param, Result> {
		Result execute(Param p);
	}

	public interface LastBlock<Param> {
		void execute(Param p);
	}

	public interface HandleBlock{
		boolean handle(Exception e);
	}
	//endregion
}
