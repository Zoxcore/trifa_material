{
  description = "Development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";
    #nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            maven
            jdk21

            libGL
            xorg.libX11
            xorg.libXcursor
            xorg.libXrandr
            xorg.libXrender
            xorg.libXext
            xorg.libXxf86vm
            fontconfig
            freetype
            libv4l
            libnotify
            libxcb
            libpulseaudio
            alsa-lib
          ];

          shellHook = ''
            export PATH="${pkgs.jdk21}/bin:$PATH"
            export _JAVA_OPTIONS="-Dskiko.linux.autodpi=false -Dsun.java2d.uiScale=2"
            export LD_LIBRARY_PATH="${pkgs.libGL}/lib:${pkgs.xorg.libX11}/lib:${pkgs.xorg.libXcursor}/lib:${pkgs.xorg.libXrandr}/lib:${pkgs.xorg.libXrender}/lib:${pkgs.xorg.libXext}/lib:${pkgs.xorg.libXxf86vm}/lib:${pkgs.fontconfig}/lib:${pkgs.freetype}/lib:${pkgs.libv4l}/lib:${pkgs.libnotify}/lib:${pkgs.libxcb}/lib:${pkgs.libpulseaudio}/lib:${pkgs.alsa-lib}/lib:$LD_LIBRARY_PATH"
          '';
        };
      }
    );
}
