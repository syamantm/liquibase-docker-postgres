package com.syamantakm.liquibase

import java.util

import com.ninja_squad.dbsetup.DbSetup
import com.ninja_squad.dbsetup.destination.DataSourceDestination
import com.ninja_squad.dbsetup.operation.Operation
import com.zaxxer.hikari.{HikariDataSource, HikariConfig}
import org.scalatest.{Matchers, FlatSpec}
import com.ninja_squad.dbsetup.Operations._
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.util.IntegerMapper
import org.slf4j.LoggerFactory

/**
 * @author syamantak.
 */
class LiquibaseRunnerTest extends FlatSpec with Matchers {
  private[this] val logger = LoggerFactory.getLogger(classOf[LiquibaseRunnerTest])

  private[this] val dataSource = setupDataSource
  private[this] val dbi = new DBI(dataSource)


  "A Database Test" should "insert data" in {
    classPathDataSet("/persons.csv") loadedToDatabase

    val handle = dbi.open()
    val rowCount = handle.createQuery("SELECT count(1) FROM person").map(IntegerMapper.FIRST).first()

    rowCount should be(2)
    handle.close()
  }

  def setupDataSource = {
    val config = new HikariConfig
    val jdbcUrl = sys.props.getOrElse("jdbc.url", "jdbc:postgresql://192.168.59.103/postgres?currentSchema=public")
    config.setJdbcUrl(jdbcUrl)
    config.setUsername("postgres")
    config.setPassword("postgres")
    new HikariDataSource(config)
  }

  def classPathDataSet(fileName: String) =
    DataSet(scala.io.Source.fromURL(getClass.getResource(fileName)).getLines)

  
  case class DataSet(lines: Iterator[String]) {
    
    def loadedToDatabase = {
      val data = new util.ArrayList[Operation]
      data.add(deleteAllFrom("person"))

      lines
        .map(a => sql(s"insert into person values($a)"))
        .foreach(insert => data.add(insert))
      
      val dbSetup = new DbSetup(new DataSourceDestination(dataSource), sequenceOf(data))
      dbSetup.launch
    }
  }
}
