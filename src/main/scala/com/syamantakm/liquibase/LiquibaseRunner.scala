package com.syamantakm.liquibase

import liquibase.integration.commandline.Main

/**
 * @author syamantak.
 */
object LiquibaseRunner extends App {

  args.foreach(println)
  Main.main(args)

}
