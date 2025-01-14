/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sumologic.elasticsearch.restlastic.dsl

trait IndexDsl extends DslCommons {
  case object CreateIndex extends RootObject {
    override def toJson: Map[String, Any] = Map()
  }

  case class Document(id: String, data: Map[String, Any]) extends EsOperation with RootObject {
    override def toJson: Map[String, Any] = data
  }

  // Op types are lowercased to avoid a conflict with the Index case class
  sealed trait OperationType { val jsonStr: String }
  case object index extends  OperationType {
    override val jsonStr: String = "index"
  }
  case object create extends OperationType {
    override val jsonStr: String = "create"
  }
  case object update extends OperationType {
    override val jsonStr: String = "update"
  }

  case class Bulk(operations: Seq[BulkOperation]) extends EsOperation with RootObject {
    override def toJson: Map[String, Any] = throw new UnsupportedOperationException
    override lazy val toJsonStr = operations.map(_.toJsonStr).mkString("", "\n", "\n")
  }

  case class BulkOperation(operation: OperationType, location: Option[(Index, Type)], document: Document) extends EsOperation {
    import EsOperation.compactJson
    override def toJson: Map[String, Any] = throw new UnsupportedOperationException
    def toJsonStr: String = {
      val doc = operation match {
        case `update` =>
          Document(document.id, Map("doc"->document.data) ++ Map("detect_noop" -> true, "doc_as_upsert" -> true))
        case _ => document
      }
      val jsonObjects = Map(operation.jsonStr ->
        (Map("_id" -> doc.id) ++ location.map { case (index, tpe) => Map("_index" -> index.name, "_type" -> tpe.name)}.getOrElse(Map()))
      )
      s"${compactJson(jsonObjects)}\n${doc.toJsonStr}"
    }
  }
}


