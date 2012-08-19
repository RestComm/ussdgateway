package org.mobicents.ussdgateway.management.console.impl;

import org.mobicents.ss7.management.console.CommandContext;
import org.mobicents.ss7.management.console.CommandHandlerWithHelp;
import org.mobicents.ss7.management.console.Tree;

/**
 * @author amit bhayani
 * 
 */
public class UssdCommandHandler extends CommandHandlerWithHelp {

	static final Tree commandTree = new Tree("ussd");
	static {
//		Node parent = commandTree.getTopNode();
//
//		Node esme = parent.addChild("esme");
//		esme.addChild("create");
//		esme.addChild("delete");
//		esme.addChild("show");
//		
//		Node set = parent.addChild("set");
//		set.addChild("scgt");
//		set.addChild("scssn");
//		set.addChild("hlrssn");
//		set.addChild("mscssn");
//		set.addChild("maxmapv");
//		
//		Node get = parent.addChild("get");
//		get.addChild("scgt");
//		get.addChild("scssn");
//		get.addChild("hlrssn");
//		get.addChild("mscssn");
//		get.addChild("maxmapv");

	};

	public UssdCommandHandler() {
		super(commandTree, CONNECT_MANDATORY_FLAG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.CommandHandler#isValid(java.lang
	 * .String)
	 */
	@Override
	public void handle(CommandContext ctx, String commandLine) {
		// TODO Validate command
		if (commandLine.contains("--help")) {
			this.printHelp(commandLine, ctx);
			return;
		}
		
		ctx.sendMessage(commandLine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.CommandHandler#isAvailable(org.mobicents
	 * .ss7.management.console.CommandContext)
	 */
	@Override
	public boolean isAvailable(CommandContext ctx) {
		if (!ctx.isControllerConnected()) {
			ctx.printLine("The command is not available in the current context. Please connnect first");
			return false;
		}
		return true;
	}

}
