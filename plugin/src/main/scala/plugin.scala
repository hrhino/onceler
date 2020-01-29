package onceler

import scala.tools.nsc._
import plugins._
import scala.tools.nsc.transform.{Transform, TypingTransformers}

class Onceler(val global: Global) extends Plugin { plugin =>
  import global._

  override val name = "onceler"
  override val description = "cache and stuff"
  override val components = deonceify :: Nil

  object defns { import definitions._
    val Once = rootMirror.getRequiredModule("onceler.once")
    val Once_apply = getMemberMethod(Once, nme.apply)
    val OnceFactory = rootMirror.getRequiredClass("onceler.internal.Factory")
    val OnceFactory_factory = getMemberMethod(OnceFactory.companionModule, sn.Bootstrap)
    val OnceFactory_dummy = OnceFactory.newMethod(nme.applyDynamic, NoPosition) setInfo NullaryMethodType(ObjectTpe)
  }
  import defns._

  object deonceify extends PluginComponent with Transform with TypingTransformers {
    override val global: plugin.global.type = plugin.global
    override val phaseName = "deonceify"
    override val runsAfter = "delambdafy" :: Nil
    override val runsBefore = "jvm" :: Nil

    override def newTransformer(unit: global.CompilationUnit) = new TypingTransformer(unit) {
      override def transform(tree: Tree): Tree = tree match {
        case Apply(fun: RefTree, Block_?(func) :: Nil) if fun.symbol == Once_apply =>
          val lmfc @ delambdafy.LambdaMetaFactoryCapable(lambdaTarget, arity, functionalInterface, sam, bridges, isSerializable, addScalaSerializableMarker) =
            func.getAndRemoveAttachment[delambdafy.LambdaMetaFactoryCapable].getOrElse {
              abort(s"once.apply call lacks LambdaMetaFactory capability???\n$tree")
            }
          assert(arity == 0, lmfc)
          val bsmRef = Literal(Constant(OnceFactory_factory))
          val staticAndDynamicArgs = Literal(Constant(lambdaTarget)) :: Nil
          ApplyDynamic(gen.mkAttributedIdent(OnceFactory_dummy), bsmRef :: staticAndDynamicArgs) setType tree.tpe
        case _ => super.transform(tree)
      }
    }
  }

  object Block_? extends treeInfo.SeeThroughBlocks[Option[Tree]] {
    override def unapplyImpl(x: Tree) = Some(x)
  }

}
