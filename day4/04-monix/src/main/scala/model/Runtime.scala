package model

import model.data.TablePlant
import monix.eval.Task
import util.FileIO

trait  Runtime[T] {
  def executeSync(env: Env = Env.Empty): Env
  def executeAsync(env: Env = Env.Empty): Task[Env] = Task {
    executeSync(env)
  }
}

trait Env
object Env {
  case object Empty extends Env
}


object RuntimeExecutors {
  case class EnvDataCsv(csvData: Option[TablePlant] = None) extends Env
  case class EnvDataModulo(moduloData: Option[TablePlant] = None) extends Env {
    def reduce(env: EnvDataModulo): EnvDataModulo = Seq(moduloData, env.moduloData).flatten match {
      case Nil => env
      case Seq(table) => EnvDataModulo(Some(table))
      case Seq(t1, t2) => EnvDataModulo(Some(TablePlant.reduce(t1, t2)))
    }
  }
  case class EnvDataForAggregation(data: Seq[Env]) extends Env

  implicit final class RawFileRuntime(rawFile: RawFile) extends Runtime[RawFile] {
    override def executeSync(env: Env = Env.Empty): Env = {
      val fileContent: TablePlant = FileIO.load(rawFile.filename)
      EnvDataCsv(csvData = Some(fileContent))
    }
  }
  implicit final class BatchModuloRuntime(batchModulo: BatchModulo) extends Runtime[BatchModulo] {
    override def executeSync(env: Env = Env.Empty): Env = {
      env match {
        case envData: EnvDataCsv =>
          EnvDataModulo(moduloData = envData.csvData.map(table => table.modulo(batchModulo.modulo, batchModulo.offset)))
        case _ => env
      }
    }
  }
  implicit final class BatchAggregateRuntime(batchAggregate: BatchAggregate) extends Runtime[BatchAggregate] {
    override def executeSync(env: Env = Env.Empty): Env = {
      env match {
        case envDataForAggregation: EnvDataForAggregation =>
          val envModulos: Seq[EnvDataModulo] = envDataForAggregation.data.collect {
            case envModulo: EnvDataModulo => envModulo
          }
          envModulos.reduce((e1, e2) => e1.reduce(e2))
        case _ => env
      }
    }
  }
}