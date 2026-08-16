{
  description = "Development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";
    #nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";
    nixpkgs-unstable.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      nixpkgs-unstable,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        pkgs-unstable = nixpkgs-unstable.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            #maven
            jdk25
            #pkgs-unstable.temurin-bin-26

            libGL
            libX11
            libXcursor
            libXrandr
            libXrender
            libXext
            libXxf86vm
            fontconfig
            freetype
            libv4l
            libnotify
            libxcb
            libpulseaudio
            alsa-lib
          ];

          shellHook = ''
            export PATH="${pkgs.jdk25}/bin:$PATH"
            #export PATH="${pkgs-unstable.temurin-bin-26}/bin:$PATH"
            export _JAVA_OPTIONS="-Dskiko.linux.autodpi=false -Dsun.java2d.uiScale=2"
            export LD_LIBRARY_PATH="${pkgs.libGL}/lib:${pkgs.libX11}/lib:${pkgs.libXcursor}/lib:${pkgs.libXrandr}/lib:${pkgs.libXrender}/lib:${pkgs.libXext}/lib:${pkgs.libXxf86vm}/lib:${pkgs.fontconfig}/lib:${pkgs.freetype}/lib:${pkgs.libv4l}/lib:${pkgs.libnotify}/lib:${pkgs.libxcb}/lib:${pkgs.libpulseaudio}/lib:${pkgs.alsa-lib}/lib:$LD_LIBRARY_PATH"
          '';
        };
      }
    );
}
