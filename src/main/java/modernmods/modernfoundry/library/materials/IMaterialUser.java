package modernmods.modernfoundry.library.materials;

import modernmods.modernfoundry.library.materials.definition.MaterialId;

/** Common interface between {@link modernmods.modernfoundry.library.materials.stats.MaterialStatsId} and {@link modernmods.modernfoundry.library.tools.part.IMaterialItem} */
public interface IMaterialUser {
  /** Checks if the given material can be used by this object. */
  boolean canUseMaterial(MaterialId material);
}
